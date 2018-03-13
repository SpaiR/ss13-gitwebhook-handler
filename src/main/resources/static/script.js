$.ajaxSetup({
    beforeSend: function(xhr) {
        xhr.setRequestHeader('X-CSRF-TOKEN', $("meta[name='_csrf']").attr('content'));
    }
});

$('document').ready(function () {
    $.ajax('/config/rest/current').done(function (data) {
        configObject = data;
        initForms();
    });


    $('#open-about-button, #close-about-button').click(function () {
        $('#overlay').fadeToggle('fast');
        $('#about-text').fadeToggle('fast');
    });


    $('.help-button').click(function () {
        var $icon = $(this).find('.material-icons');

        if ($icon.html() === 'help') {
            $icon.html('close');
        } else {
            $icon.html('help');
        }

        $('#' + $(this).data('for')).fadeToggle('fast');
    });

    $('#save-config').click(function () {
        $.ajax({
            url: '/config/rest/current',
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(configObject)
        }).done(function () {
            showToast('Configuration saved.');
            $('#save-config').prop('disabled', true);
        });
    });


    $('#validate-config').click(function () {
        $('#github-fail').hide();
        $('#changelog-fail').hide();
        $('#save-config').prop('disabled', true);

        $('#progress-bar').slideDown('fast');

        $.ajax({
            url: '/config/rest/validation',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(configObject)
        }).done(function () {
            showToast('Configuration is valid.');
            $('#save-config').prop('disabled', false);
        }).fail(function (jqXHR) {
            showToast('Error! Configuration is invalid.');

            var responseObject = JSON.parse(jqXHR.responseText);

            if (!responseObject.gitHubOk) {
                $('#github-fail').show();
            }

            if (!responseObject.changelogOk) {
                $('#changelog-fail').show();
            }
        }).always(function () {
            $('#progress-bar').slideUp('fast');
        });
    });


    $('#add-class-button').click(function () {
        addClassAction();
    });

    $('#add-class-field').keypress(function (e) {
        // React on 'enter' button.
        if (e.which === 13) {
            addClassAction();
        }
    });
});

function addClassAction() {
    var $addClassField = $('#add-class-field');
    var fieldValue = $addClassField.val();
    var classesArray = configObject.changelogConfig.html.availableClasses;

    if (fieldValue.length > 0) {
        if (classesArray.indexOf(fieldValue) !== -1) {
            showToast('Class "' + fieldValue + '" already presented in list!');
        } else {
            addClassToList(fieldValue);
            classesArray.push(fieldValue);

            $addClassField.val('');
            $addClassField.parent().get(0).MaterialTextfield.checkDirty();
        }
    }
}

function showToast(message) {
    $('#snackbar').get(0).MaterialSnackbar.showSnackbar({ message: message });
}

var configObject = {
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
            availableClassesLabels: { }
        }
    },
    changelogConfig: {
        pathToChangelog: '',
        html: {
            availableClasses: [ ]
        }
    }
};

function initForms() {
    initGeneralTabForms();
    initGitHubTabForms();
    initChangelogTabForms();

    // Check all fields for changes, to make labels float.
    $('.mdl-textfield').each(function () {
        if (this.MaterialTextfield)
            this.MaterialTextfield.checkDirty();
    });
}

function addClassToList(className) {
    $('#available-classes-list').append(
        '<li class="mdl-list__item">' +
        ' <button id="remove-class-button" data-class-to-remove="' + className + '" class="mdl-button mdl-js-button mdl-button--icon mdl-button--accent">' +
        '  <i class="material-icons">clear</i>' +
        ' </button>' +
        '&nbsp;' + className +
        '</li>'
    );

    $('[data-class-to-remove="' + className + '"]').click(function () {
        $(this).parent().remove();

        var classesArray = configObject.changelogConfig.html.availableClasses;
        classesArray.splice(classesArray.indexOf(className), 1);

        delete availableClassesLabelsMap[className];
        $classLabel.parent().parent().remove();
    });

    var availableClassesLabelsMap = configObject.gitHubConfig.labels.availableClassesLabels;

    $('#available-classes-labels').append(
        '<tr>' +
        ' <td>' + className + '</td>' +
        ' <td>' +
        '   <input data-class-label="' + className + '" class="mdl-textfield__input">' +
        ' </td>' +
        '</tr>'
    );

    var $classLabel = $('[data-class-label="' + className + '"]');

    if (availableClassesLabelsMap.hasOwnProperty(className)) {
        $classLabel.val(availableClassesLabelsMap[className]);
    } else {
        availableClassesLabelsMap[className] = '';
    }

    $classLabel.keyup(function () {
        availableClassesLabelsMap[className] = $(this).val();
    });
}

function initGeneralTabForms() {
    var $requestAgentNameInput = $('#request-agent-name');
    $requestAgentNameInput.val(configObject.requestAgentName);
    $requestAgentNameInput.keyup(function () { configObject.requestAgentName = $requestAgentNameInput.val(); });

    var $imageUploadCodeInput = $('#image-upload-code');
    $imageUploadCodeInput.val(configObject.imageUploadCode);
    $imageUploadCodeInput.keyup(function () { configObject.imageUploadCode = $imageUploadCodeInput.val(); });

    $('#time-zone').val(configObject.timeZone).change(function () { configObject.timeZone = $('#time-zone').val(); });
}

function initGitHubTabForms() {
    var $organizationName = $('#organization-name');
    $organizationName.val(configObject.gitHubConfig.organizationName);
    $organizationName.keyup(function () {
        configObject.gitHubConfig.organizationName = $organizationName.val();
        $('#save-config').prop('disabled', true);
    });

    var $repositoryName = $('#repository-name');
    $repositoryName.val(configObject.gitHubConfig.repositoryName);
    $repositoryName.keyup(function () {
        configObject.gitHubConfig.repositoryName = $repositoryName.val();
        $('#save-config').prop('disabled', true);
    });

    var $apiToken = $('#api-token');
    $apiToken.val(configObject.gitHubConfig.token);
    $apiToken.keyup(function () { configObject.gitHubConfig.token = $apiToken.val() });

    var $secretKey = $('#secret-key');
    $secretKey.val(configObject.gitHubConfig.secretKey);
    $secretKey.keyup(function () { configObject.gitHubConfig.secretKey = $secretKey.val() });

    var $invalidChangelog = $('#invalid-changelog');
    $invalidChangelog.val(configObject.gitHubConfig.labels.invalidChangelog);
    $invalidChangelog.keyup(function () { configObject.gitHubConfig.labels.invalidChangelog = $invalidChangelog.val() });

    var $mapChanges = $('#map-changes');
    $mapChanges.val(configObject.gitHubConfig.labels.mapChanges);
    $mapChanges.keyup(function () { configObject.gitHubConfig.labels.mapChanges = $mapChanges.val() });

    var $iconChanges = $('#icon-changes');
    $iconChanges.val(configObject.gitHubConfig.labels.iconChanges);
    $iconChanges.keyup(function () { configObject.gitHubConfig.labels.iconChanges = $iconChanges.val() });

    var $workInProgress = $('#work-in-progress');
    $workInProgress.val(configObject.gitHubConfig.labels.workInProgress);
    $workInProgress.keyup(function () { configObject.gitHubConfig.labels.workInProgress = $workInProgress.val() });

    var $doNotMerge = $('#do-not-merge');
    $doNotMerge.val(configObject.gitHubConfig.labels.doNotMerge);
    $doNotMerge.keyup(function () { configObject.gitHubConfig.labels.doNotMerge = $doNotMerge.val() });
}

function initChangelogTabForms() {
    var $pathToChangelog = $('#path-to-changelog');
    $pathToChangelog.val(configObject.changelogConfig.pathToChangelog);
    $pathToChangelog.keyup(function () {
        configObject.changelogConfig.pathToChangelog = $pathToChangelog.val();
        $('#save-config').prop('disabled', true);
    });

    configObject.changelogConfig.html.availableClasses.forEach(function (item) {
        addClassToList(item);
    });
}