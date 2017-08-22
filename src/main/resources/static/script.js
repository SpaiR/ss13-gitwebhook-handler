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


    $('#save-config').click(function () {
        $.ajax({
            url: '/config/rest/current',
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(configObject)
        }).done(function () {
            showToast('Configuration saved');
            $('#save-config').prop('disabled', true);
        });
    });


    $('#validate-config').click(function () {
        $('#github-fail').hide();
        $('#changelog-fail').hide();
        $('#save-config').prop('disabled', true);

        if ($('#path-to-changelog').val().charAt(0) !== '/') {
            showToast('Warning! Changelog path should start with "/"');
        } else {
            $('#progress-bar').slideDown('fast');

            $.ajax({
                url: '/config/rest/validation',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(configObject)
            }).done(function () {
                showToast('Configuration is valid');
                $('#save-config').prop('disabled', false);
            }).fail(function (jqXHR) {
                showToast('Error! Configuration is invalid');

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
        }
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

    if (fieldValue.length > 0) {
        addClassToList(fieldValue, configObject.changelogConfig.html.availableClasses.length);
        configObject.changelogConfig.html.availableClasses.push(fieldValue);

        $addClassField.val('');
        $addClassField.parent().get(0).MaterialTextfield.checkDirty();
    }
}

function showToast(message) {
    $('#snackbar').get(0).MaterialSnackbar.showSnackbar({ message: message });
}

var configObject = {
    requestAgentName: '',
    timeZone: '',
    gitHubConfig: {
        organizationName: '',
        repositoryName: '',
        token: '',
        secretKey: ''
    },
    changelogConfig: {
        pathToChangelog: '',
        html: {
            moreText: '',
            updateText: '',
            availableClasses: [ ]
        }
    }
};

function initForms() {
    var $requestAgentNameInput = $('#request-agent-name');
    $requestAgentNameInput.val(configObject.requestAgentName);
    $requestAgentNameInput.keyup(function () { configObject.requestAgentName = $requestAgentNameInput.val(); });

    $('#time-zone').val(configObject.timeZone).change(function () { configObject.timeZone = $('#time-zone').val(); });

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

    var $pathToChangelog = $('#path-to-changelog');
    $pathToChangelog.val(configObject.changelogConfig.pathToChangelog);
    $pathToChangelog.keyup(function () {
        configObject.changelogConfig.pathToChangelog = $pathToChangelog.val();
        $('#save-config').prop('disabled', true);
    });

    var $htmlMoreText = $('#html-more-text');
    $htmlMoreText.val(configObject.changelogConfig.html.moreText);
    $htmlMoreText.keyup(function () { configObject.changelogConfig.html.moreText = $htmlMoreText.val() });

    var $htmlUpdateText = $('#html-update-text');
    $htmlUpdateText.val(configObject.changelogConfig.html.updateText);
    $htmlUpdateText.keyup(function () { configObject.changelogConfig.html.updateText = $htmlUpdateText.val() });

    configObject.changelogConfig.html.availableClasses.forEach(function (item, index) {
        addClassToList(item, index);
    });

    // Check all fields for changes, to make labels float.
    $('.mdl-textfield').each(function () { this.MaterialTextfield.checkDirty(); });
}

function addClassToList(className, index) {
    $('#available-classes-list').append(
        '<li data-class-index="' + index + '" class="mdl-list__item">' +
        ' <button id="remove-class-button" data-index-to-remove="' + index + '" class="mdl-button mdl-js-button mdl-button--icon mdl-button--accent">' +
        '  <i class="material-icons">clear</i>' +
        ' </button>' +
        '&nbsp;' + className +
        '</li>'
    );

    $('#remove-class-button[data-index-to-remove="' + index + '"]').click(function () {
        $('#available-classes-list').find('li[data-class-index="' + index + '"]').remove();

        var classesArray = configObject.changelogConfig.html.availableClasses;
        classesArray.splice(classesArray.indexOf(className), 1);
    });
}