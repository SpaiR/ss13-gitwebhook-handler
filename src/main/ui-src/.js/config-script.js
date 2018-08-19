$.ajaxSetup({
    beforeSend: xhr => {
        xhr.setRequestHeader('X-CSRF-TOKEN', $("meta[name='_csrf']").attr('content'));
    }
});

let controller = {
    disableSaveBtn: () => {
        toggleSaveButton(false);
    },

    enableSaveBtn: () => {
        toggleSaveButton(true);
    },

    removeClass: (event, rivetsBinding) => {
        let className = rivetsBinding.item,
            classesArray = config.changelogConfig.html.availableClasses;

        classesArray.splice(classesArray.indexOf(className), 1);

        // It would be better to place this in 'label-for-class-value' declaration,
        // but RivetsJs doesn't give ability to do it in a natural way, so now it's here.
        delete config.labels.labelsForClasses[className];
    },

    removeMasterUser: (event, rivetsBinding) => {
        let userName = rivetsBinding.item,
            usersArray = config.gitHubConfig.masterUsers;

        usersArray.splice(usersArray.indexOf(userName), 1);
    }
}, config = {
    requestAgentName: '',
    timeZone: '',
    imageUploadCode: '',
    handlerUrl: '',
    gitHubConfig: {
        organizationName: '',
        repositoryName: '',
        token: '',
        secretKey: '',
        masterUsers: []
    },
    labels: {
        invalidChangelog: '',
        mapChanges: '',
        iconChanges: '',
        workInProgress: '',
        doNotMerge: '',
        testMerge: '',
        interactiveDiffMap: '',
        labelsForClasses: {}
    },
    changelogConfig: {
        pathToChangelog: '',
        html: {
            availableClasses: []
        }
    },
    dmmBotConfig: {
        pathToDme: ''
    }
};

$.ajax('/config/rest/current').done(data => {
    config = data;

    $(document).ready(() => {
        initRivets();
        initForms();
    });
});

$(document).ready(() => {
    initDmmBot();


    $('#open-about-button, #close-about-button').click(() => {
        $('#overlay').fadeToggle('fast');
        $('#about-block').fadeToggle('fast');
    });


    $('.help__button').click(function () {
        const CLOSE_ICON = 'close', HELP_ICON = 'help';
        let $icon = $(this).find('.material-icons');

        if ($icon.html() === HELP_ICON) {
            $icon.html(CLOSE_ICON);
        } else {
            $icon.html(HELP_ICON);
        }

        $('#' + $(this).data('for')).fadeToggle('fast');
    });

    $('#save-config').click(() => {
        $.ajax({
            url: '/config/rest/current',
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(config)
        }).done(() => {
            showToast('Configuration saved.');
            toggleSaveButton(false);
        });
    });


    $('#validate-config').click(() => {
        let $githubIcon = $('#github-fail'),
            $changelogIcon = $('#changelog-fail'),
            $dmmBotIcon = $('#dmm-bot-fail'),
            $progressBar = $('#progress-bar');

        $githubIcon.hide();
        $changelogIcon.hide();
        $dmmBotIcon.hide();
        toggleSaveButton(false);

        $progressBar.slideDown('fast');

        $.ajax({
            url: '/config/rest/validation',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(config)
        }).done(() => {
            showToast('Configuration is valid.');
            toggleSaveButton(true);
        }).fail(jqXHR => {
            showToast('Error! Configuration is invalid.');

            let responseObject = JSON.parse(jqXHR.responseText);

            if (!responseObject.gitHubOk) {
                $githubIcon.show();
            }

            if (!responseObject.changelogOk) {
                $changelogIcon.show();
            }

            if (!responseObject.dmmBotOk) {
                $dmmBotIcon.show();
            }
        }).always(() => {
            $progressBar.slideUp('fast');
        });
    });


    $('#add-class-button').click(() => {
        addClassAction();
    });

    $('#add-class-field').keypress(e => {
        // React on 'enter' button.
        if (e.which === 13) {
            addClassAction();
        }
    });


    $('#add-master-user-button').click(() => {
        addMasterUserAction();
    });

    $('#add-master-user-field').keypress(e => {
        // React on 'enter' button.
        if (e.which === 13) {
            addMasterUserAction();
        }
    });
});

function toggleSaveButton(isEnable) {
    $('#save-config').prop('disabled', !isEnable);
}

function addClassAction() {
    addFieldValueToArray('#add-class-field', config.changelogConfig.html.availableClasses);
}

function addMasterUserAction() {
    addFieldValueToArray('#add-master-user-field', config.gitHubConfig.masterUsers);
}

function addFieldValueToArray(fieldSelector, arrayToAdd) {
    let $addField = $(fieldSelector),
        fieldValue = $addField.val();

    if (fieldValue.length > 0) {
        if (arrayToAdd.indexOf(fieldValue) !== -1) {
            showToast('Item "' + fieldValue + '" already present in the list!');
        } else {
            arrayToAdd.push(fieldValue);

            $addField.val('');
            $addField.parent().get(0).MaterialTextfield.checkDirty();
        }
    }
}

function showToast(message) {
    $('#snackbar').get(0).MaterialSnackbar.showSnackbar({message: message});
}

function initDmmBot() {
    const $initMasterBtn = $('#init-master-repo'),
        $initMasterDone = $('#init-master-repo .done'),
        $initMasterProcess = $('#init-master-repo .process'),
        $initMasterFail = $('#init-master-repo .fail'),

        $cleanReposBtn = $('#clean-repos'),
        $cleanReposProcess = $('#clean-repos .process'),

        REPOS_MASTER_URL = '/config/rest/repos/master',
        REPOS_URL = '/config/rest/repos';

    function toggleInitBtn(isEnable) {
        $initMasterBtn.prop('disabled', !isEnable);
    }

    function toggleCleanBtn(isEnable) {
        $cleanReposBtn.prop('disabled', !isEnable);
    }

    (function getMasterInitStatus() {
        toggleInitBtn(false);

        $.ajax({
            url: REPOS_MASTER_URL,
            method: 'GET'
        }).done(status => {
            if (status === 'DONE') {
                $initMasterDone.show();
            } else if (status === 'NOT_STARTED') {
                $initMasterFail.show();
            }
            if (status !== 'IN_PROGRESS') {
                $initMasterProcess.hide();
                toggleInitBtn(true);
                toggleCleanBtn(true);
            } else {
                toggleInitBtn(false);
                toggleCleanBtn(false);
                setTimeout(getMasterInitStatus, 5000);
            }
        });
    })();

    $initMasterBtn.click(() => {
        $initMasterDone.hide();
        $initMasterFail.hide();
        $initMasterProcess.show();
        toggleCleanBtn(false);
        toggleInitBtn(false);

        $.ajax({
            url: REPOS_MASTER_URL,
            method: 'PUT'
        }).done(() => {
            toggleInitBtn(true);
            toggleCleanBtn(true);
        }).fail(() => {
            $initMasterFail.show();
            $initMasterProcess.hide();
            toggleInitBtn(true);
            toggleCleanBtn(true);
            showToast('Error on master repo initialization.')
        });
    });

    $cleanReposBtn.click(() => {
        $cleanReposProcess.show();
        toggleCleanBtn(false);
        toggleInitBtn(false);

        $.ajax({
            url: REPOS_URL,
            method: 'DELETE'
        }).done(() => {
            showToast('All repos deleted.');
            $initMasterDone.hide();
            $initMasterFail.show();
        }).always(() => {
            toggleCleanBtn(true);
            toggleInitBtn(true);
            $cleanReposProcess.hide();
        });
    });
}

function initForms() {
    // Check all fields for changes, to make labels float.
    $('.mdl-textfield').each(function () {
        if (this.MaterialTextfield)
            this.MaterialTextfield.checkDirty();
    });
}

function initRivets() {
    rivets.binders['label-for-class-value'] = {
        routine: function (el, value) {
            this.className = value;
            const labelsForClassesMap = config.labels.labelsForClasses;

            if (labelsForClassesMap.hasOwnProperty(value)) {
                el.value = labelsForClassesMap[value];
            } else {
                el.value = labelsForClassesMap[value] = '';
            }
        },

        bind: function (el) {
            const that = this;
            $(el).keyup(() => {
                config.labels.labelsForClasses[that.className] = el.value;
            });
        }
    };

    rivets.bind(document.body, {config: config, ctrl: controller});
}