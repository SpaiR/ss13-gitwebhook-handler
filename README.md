[![Build Status](https://travis-ci.org/SpaiR/ss13-gitwebhook-handler.svg?branch=master)](https://travis-ci.org/SpaiR/ss13-gitwebhook-handler)

# SS13 Gitwebhook Handler

## About

SS13 Gitwebhook Handler is an automation tool for maintaining [TauCeti](https://github.com/TauCetiStation/TauCetiClassic) repository. 
This handler is built with orientation on [BYOND](http://www.byond.com/) games, mainly for Space Station 13,
so with minor changes it could be used for any compatible codebase.

Handler features:
 - Changelog generation and validation from PR description markup
 - `DMM` and `DMI` diff reports
 - Test Merge changelog generation
 - Auto-labeling of PRs and Issues
 - Web UI interface (Configuration without recompiling or redeploying)

Tool may look similar to `github_webhook_processor.php`, which do sort of same things,
but unlike it this handler gives more complex and flexible solution, which could be extended in any way, without maintenance problems.

## Installation

**Important:** application needs Java (at least 8 version).

1. [Download](https://github.com/SpaiR/ss13-gitwebhook-handler/releases) 'jar' file.
2. Start it with any next command:
    - `java -jar ss13-gitwebhook-handler-${version}.jar --security.user.name=${login} --security.user.password=${password}`
    - `java -jar ss13-gitwebhook-handler-${version}.jar --server.port=${port} --security.user.name=${login} --security.user.password=${password}`
3. Go to configuration UI in browser and change all properties as you need. Config page located on address: `/config`
4. Configure your repository to send webhook to handler.

While filling of some fields questions about what value it should contain may appear. 
In the right part of the page full description about every field can be found. (Question mark in the top-right part.)
Additional info may be found in `About` window. (Info mark in the bottom-right part.)

Also, during application startup two files will be created: GWHLog.log and GWHConfig.json for logs and configuration storing.

### GitHub configuration

To work handler should receive so called webhooks from GitHub and it should have token to interact with GitHub API.
 - Webhook should send `pull_request` and `issues` events
 - Webhook should send data in `application/json` format
 - GitHub API token should have at least `repo` rights

Endpoint to send webhooks: `/handler`

### Image Sharing Server

To store images handler use self hosted image sharing service [PictShare](https://github.com/chrisiaut/pictshare) with `upload_code` check.
Current configured endpoint is `img.taucetistation.org`. It can't be changed without app recompiling.

## How To Work With...

### ...Changelog Generation

To patch `.html` file with new changes it **should** have `<div id="changelogs"></div>` element. For example:
```
<div id="changelogs"></div>
```

All the rest will be generated automatically.

Example of PR description with changelog markup:
```
...Some PR description...
:cl:
 - bugfix: Lorem ipsum dolor sit amet, consectetur adipiscing elit.
```

After pull request will be merged next will appear in changelog file:
```
<!doctype html>
<html>
<head></head>
<body>
  <div id="changelogs">
    <div class="row" data-date="${current date}">
      <div class="col-lg-12">
        <h3 class="row-header">${current date}</h3>
        <div data-author="${author name}">
          <h4 class="author">${author name}:</h4>
          <ul class="changelog">
            <li class="bugfix">Lorem ipsum dolor sit amet, consectetur adipiscing elit.</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
```

#### About syntax

1. All text after `:cl:`will be parsed as changelog, so it should be in the end of PR body.
2. Changelog entry is a list entry in GitHub markdown. So, if your changelog does not look like a list it's probably invalid.
3. Entry should not contain explicit newlines. If entry is too long GitHub may visually stretch it, but it's fine.
4. ` - class: Text.` Space before dash, after it and after colon.

#### Custom Author name

By default handler takes author name value as GitHub login. This could be modified by setting custom name after `:cl:` symbol.
```
...Some PR description...
:cl: Custom name goes here
 - bugfix: Lorem ipsum dolor sit amet, consectetur adipiscing elit.
```

#### PR linking

Adding of `[link]` tag after changelog class will result into linking with PR, by adding link button.

This: ` - bugfix[link]: Lorem ipsum dolor sit amet, consectetur adipiscing elit.`
Will result into this:
 ```
 <li class="bugfix">Lorem ipsum dolor sit amet, consectetur adipiscing elit.<a class="btn btn-xs btn-success link-btn" href="PR url">Read More</a></li>
 ```

### ...Validation

There is no need to work with it directly, all process is automatic.
If changelog markup is invalid, handler will mark PR with `Invalid Changelog` label (can be changed in Web UI)
and leave comment with problem description. After markup fixing, label will be removed automatically.

Validation process happens on PR creation and description editing.

### ...Auto-labeling

Auto-labeling works with pull requests and issues. Process happens **only once** on issue/pull request creation.

#### Issues

All issues would automatically labeled with `Bug` label. Issue with `[Proposal]` tag in title will be labeled with `Proposal` label.

#### Pull Requests

List of labels to add to PR depends on three things:
 1. If PR diff has changes in `.dmi` or `.dmm` files `Icon Changes` and `Map Changes` labels will be added.
 2. If `[DNM]` or `[WIP]` tags exists in title `Do Not Merge` and `Work In Progress` labels will be added.
 3. Changelog classes are used to determine label to add.
 
Read more in Web UI.

### ...Dmi / Dmm Diff Report

If handler found changes in `.dmi`/`.dmm` files, diff report will be generated automatically.
Report generation happens on PR creation and new commits pushing events.

### ... Test Merge Changelog generation

Some pull requests may be merged locally for test merge. Since pull request doesn't merged in github, changelog, if exist, won't be generated.
To notify players about changes there is ability to create Test Merge changelog, by adding "Test Merge" label.
This changelog will be automatically removed after PR merging or label removing.

<hr />

Current work of handler can be seen here [TauCetiStation/TauCetiClassic](https://github.com/TauCetiStation/TauCetiClassic).
