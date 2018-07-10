$.ajaxSetup({
    beforeSend: function(xhr) {
        xhr.setRequestHeader('X-CSRF-TOKEN', $("meta[name='_csrf']").attr('content'));
    }
});

var controller = {
    disableSaveBtn: function() {
        toggleSaveButton(false);
    },

    enableSaveBtn: function() {
        toggleSaveButton(true);
    },

    removeClass: function(event, rivetsBinding) {
        var className = rivetsBinding.item,
            classesArray = config.changelogConfig.html.availableClasses;

        classesArray.splice(classesArray.indexOf(className), 1);

        // It would be better to place this in 'label-for-class-value' declaration,
        // but RivetsJs doesn't give ability to do it in a natural way, so now it's here.
        delete config.gitHubConfig.labels.labelsForClasses[className];
    }
}, config = {
    requestAgentName: '',
    timeZone: '',
    imageUploadCode: '',
    gitHubConfig: {
        organizationName: '',
        repositoryName: '',
        token: '',
        secretKey: '',
        labels: {
            invalidChangelog: '',
            mapChanges: '',
            iconChanges: '',
            workInProgress: '',
            doNotMerge: '',
            labelsForClasses: { }
        }
    },
    changelogConfig: {
        pathToChangelog: '',
        html: {
            availableClasses: [ ]
        }
    },
    dmmBotConfig: {
        pathToDme: ''
    }
};

$.ajax('/config/rest/current').done(function(data) {
    config = data;

    $(document).ready(function() {
        initRivets();
        initForms();
    });
});

$(document).ready(function() {
    initDmmBot();


    $('#open-about-button, #close-about-button').click(function() {
        $('#overlay').fadeToggle('fast');
        $('#about-block').fadeToggle('fast');
    });


    $('.help__button').click(function() {
        var $icon = $(this).find('.material-icons'),
            CLOSE_ICON = 'close', HELP_ICON = 'help';

        if ($icon.html() === HELP_ICON) {
            $icon.html(CLOSE_ICON);
        } else {
            $icon.html(HELP_ICON);
        }

        $('#' + $(this).data('for')).fadeToggle('fast');
    });

    $('#save-config').click(function() {
        $.ajax({
            url: '/config/rest/current',
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(config)
        }).done(function() {
            showToast('Configuration saved.');
            toggleSaveButton(false);
        });
    });


    $('#validate-config').click(function() {
        var $githubIcon = $('#github-fail'),
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
        }).done(function() {
            showToast('Configuration is valid.');
            toggleSaveButton(true);
        }).fail(function(jqXHR) {
            showToast('Error! Configuration is invalid.');

            var responseObject = JSON.parse(jqXHR.responseText);

            if (!responseObject.gitHubOk) {
                $githubIcon.show();
            }

            if (!responseObject.changelogOk) {
                $changelogIcon.show();
            }

            if (!responseObject.dmmBotOk) {
                $dmmBotIcon.show();
            }
        }).always(function() {
            $progressBar.slideUp('fast');
        });
    });


    $('#add-class-button').click(function() {
        addClassAction();
    });

    $('#add-class-field').keypress(function(e) {
        // React on 'enter' button.
        if (e.which === 13) {
            addClassAction();
        }
    });
});

function toggleSaveButton(isEnable) {
    $('#save-config').prop('disabled', !isEnable);
}

function addClassAction() {
    var $addClassField = $('#add-class-field'),
        fieldValue = $addClassField.val(),
        classesArray = config.changelogConfig.html.availableClasses;

    if (fieldValue.length > 0) {
        if (classesArray.indexOf(fieldValue) !== -1) {
            showToast('Class "' + fieldValue + '" already present in the list!');
        } else {
            classesArray.push(fieldValue);

            $addClassField.val('');
            $addClassField.parent().get(0).MaterialTextfield.checkDirty();
        }
    }
}

function showToast(message) {
    $('#snackbar').get(0).MaterialSnackbar.showSnackbar({ message: message });
}

function initDmmBot() {
    var $initMasterBtn = $('#init-master-repo'),
        $initMasterDone = $('#init-master-repo .done'),
        $initMasterProcess = $('#init-master-repo .process'),
        $initMasterFail = $('#init-master-repo .fail'),

        $cleanReposBtn = $('#clean-repos'),
        $cleanReposProcess = $('#clean-repos .process'),

        REPOS_MASTER_URL = '/config/rest/repos/master',
        REPOS_URL = '/config/rest/repos';

    function toggleCleanBtn(isEnable) {
        $cleanReposBtn.prop('disabled', !isEnable);
    }        

    (function getMasterInitStatus() {
        $.ajax({
            url: REPOS_MASTER_URL,
            method: 'GET'
        }).done(function(status) {
            if (status == 'DONE') {
                $initMasterDone.show();
            } else if (status === 'NOT_STARTED') {
                $initMasterFail.show();
            }
            if (status !== 'IN_PROGRESS') {
                $initMasterProcess.hide();
                toggleCleanBtn(true);
            } else {
                toggleCleanBtn(false);
                setTimeout(getMasterInitStatus, 5000);
            }
        });
    })();

    $initMasterBtn.click(function() {
        $initMasterFail.hide();
        $initMasterProcess.show();
        toggleCleanBtn(false);

        $.ajax({
            url: REPOS_MASTER_URL,
            method: 'PUT'
        }).done(function() {
            toggleCleanBtn(true);
        })
    });

    $cleanReposBtn.click(function() {
        $cleanReposProcess.show();

        $.ajax({
            url: REPOS_URL,
            method: 'DELETE'
        }).done(function() {
            showToast('All repos deleted.');
            $initMasterDone.hide();
            $initMasterFail.show();
        }).always(function() {
            $cleanReposProcess.hide();
        });
    });
}

function initForms() {
    // Check all fields for changes, to make labels float.
    $('.mdl-textfield').each(function() {
        if (this.MaterialTextfield)
            this.MaterialTextfield.checkDirty();
    });
}

function initRivets() {
    rivets.binders['label-for-class-value'] = {
        routine: function(el, value) {
            this.className = value;
            var labelsForClassesMap = config.gitHubConfig.labels.labelsForClasses;

            if (labelsForClassesMap.hasOwnProperty(value)) {
                el.value = labelsForClassesMap[value];
            } else {
                el.value = labelsForClassesMap[value] = '';
            }
        },

        bind: function(el) {
            var that = this;
            $(el).keyup(function() {
                config.gitHubConfig.labels.labelsForClasses[that.className] = el.value;
            });
        }
    };

    rivets.bind(document.body, { config: config, ctrl: controller });
}