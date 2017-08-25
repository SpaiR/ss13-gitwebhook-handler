# ss13-gitwebhook-handler
## About
GitHub webhook handler, oriented for Space Station 13 repositories.
Actually, this orientation isn't strict, 
so handler could be used for any SS13 like repositories based on BYOND.

#### What it can do:
- Create changelog from PR description
- Validate changelog on PR creation and edit

## Features
- Web UI configuration.
There is no need to recompile application to work with another repository.
All properties could be changed in browser. Screenshots: http://imgur.com/a/Z8tQa
- Works with just one jar.
This application based on Spring Boot framework, 
so literally everything can work with just one 'jar' and command line to start it.

## Installation
**Before:** ensure that machine where you gonna start app has at least Java 8.

1. [Download](https://github.com/SpaiR/ss13-gitwebhook-handler/releases) 'jar' archive to your server.
2. Start it with next command:
`java -jar ss13-gitwebhook-handler-1.0.jar --security.user.name=[your login name] --security.user.password=[your login password]`
In root where you started the app will be created two files: GWHConfig.json and GWHLog.log for configuration and logging respectively.
3. Go to configuration UI in browser and change all properties as you need.
Path should look like this: `[server ip]:[port]/config` or `[server dns]/config`

For additional info about what every field mean, look description in the right part or click help button in the bottom-right.

## Additional info
### Changelog Generator
This generator **NOT** oriented to work with legacy changelog html's.
By legacy I mean [these](http://i.imgur.com/zNf32aG.png) Web 1.0 design.
So, instead, generator was designed to work with [this](http://i.imgur.com/C6pHaOu.png).

To generate changelog there is `<div id="changelogs"></div>` element should exist.

Result schema will be:
```
<div id="changelogs">
 <div class="row" id="[current date]"> 
  <div class="col-lg-12">
   <h3 class="date">[current date]</h3> 
   <div id="[author name]"> 
    <h4 class="author">[author name] [updated text from config]:</h4> 
    <ul class="changelog"> 
     <li class="rscadd">Lorem ipsum dolor sit amet, consectetur adipiscing elit.</li> 
    </ul> 
   </div> 
  </div> 
 </div> 
</div>
```

#### How to use:
**At the end** of PR description add:
```
:cl: [here custom author name could be passed]
 - rscadd: Lorem ipsum dolor sit amet, consectetur adipiscing elit.

or

:cl:
- rscadd: Lorem ipsum dolor sit amet, consectetur adipiscing elit.
```
**Important:** to be valid there should be space after first `-` and after `:` sign. Also it will look like list in GitHub markdown.

After PR is merged changelog html file will be updated instantly.

Generator can automatically add link to PR if `[link]` mark added to change description. Like that:
```
:cl:
 - rscadd[link]: Lorem ipsum dolor sit amet, consectetur adipiscing elit.
```
and this will result into next entry: `<li class="rscadd">Lorem ipsum. <a href="[link to PR]">- [more text from config] -</a></li> `

Generator will also automatically do next things:
- Capitalize first letter of change description.
- Add dot to the end of description if there is no `.`, `?` or `!`.

HTML, CSS and all used scripts for changelog file provided in template folder.
How it looks in production can be seen [here](https://github.com/TauCetiStation/TauCetiClassic/blob/master/html/changelog.html).

#### Validation:
After PR created or edited, if 'in body' changelog exist, it will be validated.
To make process right your repository **should** have `Invalid Changelog` label.
If changelog validation failed this label will be automatically added to PR. Also comment with fail description will be created.
Right after changelog fixed or removed, warning label will be removed too.
