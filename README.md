# SS13 Gitwebhook Handler
## About
Gitwebhook Handler is an automation tool, oriented on GitHub repositories with any kind of game built on [BYOND](http://www.byond.com/).
Mainly for Space Station 13 repositories. It can do next things:
- Parse pull request body description for special markup and create from it changelog, 
with instant modification of `.html` file with actual changelog. No need of additional scripts and etc.
- Validate changelog written in pull request and notify if any mistake found.
- Auto-label issues and pull requests.

Also, unlike similar tool called `github_webhook_processor.php`, which do sort of same things,
this handler has [**Web UI**](https://imgur.com/a/Z8tQa) and easy way configuration handling, without redeploying and other annoying stuff.

## Installation
**Before:** ensure that machine where you gonna start the app has installed at least Java 8.

1. [Download](https://github.com/SpaiR/ss13-gitwebhook-handler/releases) 'jar' archive to your server.

2. Start it with next command:
    - `java -jar ss13-gitwebhook-handler-1.0.jar --security.user.name=[your login name] --security.user.password=[your login password]`
    
   In root where you started the app two files will be created: `GWHConfig.json` and `GWHLog.log` for configuration and logging store respectively.

3. Go to configuration UI in browser and change all properties as you need. Path to UI should look like this:
   - `[server ip]:[port]/config` or `[server dns]/config`

4. Config your Repo to send webhook to handler.

While filling of some fields questions about what value it should contain may appear. 
In the right part of the page (believe me, you won't miss it) you can find pretty full description about every field.
Also, additional info may be found in `About` window. Click question mark in the bottom-right part of the footer to see it.

### GitHub configuration
To do something handler should receive so called webhooks from GitHub. (That is why it's called webhook handler...)
I won't go into details about how to create them, but it's important to tell, that:
- Your webhook should send `pull_request` and `issues` events.
- Your GitHub API token should has at least `repo` rights.

URL to send webhook is next: `[server ip]:[port]/handler` or `[server dns]/handler`.

## How To Work With...
### ...Changelog Generation
To patch your `.html` file with new changes it **should** has `<div id="changelogs"></div>` element.
For example:
```
<!doctype html>
<html>
<head></head>
<body>
  <div id="changelogs"></div>
</body>
</html>
```

Everything inside it will be generated automatically.

Example of PR description with changelog markup:
```
...Some PR description...
:cl:
- bugfix: Lorem ipsum dolor sit amet, consectetur adipiscing elit.
```

After pull request merge next will appear in your changelog file:
```
<!doctype html>
<html>
<head></head>
<body>
  <div id="changelogs">
    <div class="row" data-date="${current date}">
      <div class="col-lg-12">
        <h3 class="date">${current date}</h3>
        <div data-author="${author name}">
          <h4 class="author">${author name} ${'updated' text from config}:</h4>
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

`author name` value by default is GitHub nickname of contributor. Custom name can be provided in a next way:
```
...Some PR description...
:cl: Custom name goes here
- bugfix: Lorem ipsum dolor sit amet, consectetur adipiscing elit.
```

**Note:** to be valid markup should contain space after `-` and `:`, and after GitHub procession it should look like list.

### ...Validation
There is no need to work directly with it.
If changelog markup is invalid handler will mark PR with `Invalid Changelog` label (can be changed in Web UI)
and leave comment with problem description. After markup fix label will be removed automatically.

Validation process happens on PR creation and description edit.

### ...Auto-labeling
Auto-labeling works with pull requests and issues. Process happens only once on issue/pull request creation.

#### Issues
All issues would automatically labeled with `Bug` label. Issue with `[Proposal]` tag in title will be labeled with `Proposal` label.

#### Pull Requests
List of labels to add to PR depends on three things:
1. If PR diff has changes in `.dmi` or `.dmm` files `Icon Changes` and `Map Changes` labels will be added.
2. If `[DNM]` or `[WIP]` tags exists in title `Do Not Merge` and `Work In Progress` labels will be added.
3. Changelog classes are used to determine label. Read more in Web UI.

Every label for can be configured.

<hr>

Current work of handler can be seen here [TauCetiStation/TauCetiClassic](https://github.com/TauCetiStation/TauCetiClassic).