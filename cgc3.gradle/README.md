
# cgc3

This is a (Gradle) system to (invoke *just* `g++` and) build C/++ (et al) uniformly across Win/Lin/Mac either from the command line or using (*just* `gdb` and) VCode as the GUI/IDE.

- everything requires;
  - `java` / `javac`; I'm using `java version "10.0.1"`
  - `g++`; I'm using `gcc version 7.2.0`
- editing with Visual Code requires;
  - Visual Code; I'm using whatever the rolling version is ... hmm ...
  - (to debug) `gdb` I'm using `GNU gdb (GDB) 7.10.1`
  - (optionally) Visual Code's C/++ stuff;

If the commands `javac` `g++` and `gdb` work from your command prompt; you're good to go.

# Sales Pitch (why you might *buy-in* and use this)

- really Gradle build
- really native build
- VCode tasks; generates `.vscode/tasks.json` to invoke gradle tasks
  - uses a tailored [`problemMatcher`](https://code.visualstudio.com/docs/editor/tasks#_defining-a-problem-matcher)
- VCode debug; generates `.vscode/launch.json` to run test/application targets
  - does some shenanigans to get a meaningful absolute path
- incremental rebuild; uses `g++ -MMD ...` to avoid recompilation of C/++

# System Manual (how to do what the author expected you would want to do)

The tool doesn't likely to conform to any internal conventions; you've been warned!

I tried to the interfaces of Gradle and Visual Code, but;

- the `task.json` and `launch.json` files just call out to the gradle wrapper
  - ... and utilise their own problem matchers
- I couldn't work out how to participate in `sourceSets` so ...
  - there's no `main` / `test` Janus dichotomy; test projects are just tests and the only tests
  - I've yet to implement any dependency mechanisms
  - you really, really need to work as a multi-module build

Let's walk through usage ...

## Build Script

Start by creating/copying an empty Gradle Wrapper project; I'm assuming it's a Gradle 4.8 project.
List your `library` `binary` and `check` subprojects in the `settings.gradle` file.

Assuming that you have your root project, and, `:shared` `:binary`, and `:checks` ones, your `.build.gradle` would look something like this;

```
  buildscript {
    def version = {
      "default-SNAPSHOT"
    }
    repositories {
      maven { url "https://peterlavalle.github.io/m2-repo/" }
      mavenCentral()
      jcenter()
    }
    dependencies {
      classpath "com.peterlavalle:cgc3-plugins:${version}"
    }
  }
  apply plugin: 'peterlavalle.cgc3-solution'
  project(':shared') {
    apply plugin: 'peterlavalle.cgc3-library'
  }
  project(':binary') {
    apply plugin: 'peterlavalle.cgc3-binary'

    cgc3 {
      lib project(':shared')
    }
  }
  project(':checks') {
    apply plugin: 'peterlavalle.cgc3-check'
    cgc3 {
      lib project(':shared')
    }
  }
```

That should be set up.
Once you're confident that it's running, you can (obviously) add more modules as needed.

## Command Line

Running `./gradlew tasks` will list tasks in the project, and, should now show 
By default, `peterlavalle.cgc3-solution` sets the `defaultTasks` to run the `solution` task and generate the `.vscode` files used to invoke things.

## Visual Code

Start by opening the project's folder in Visual Code as a folder.
If there's no `.vscode` folder, run the `gradelw` to generate it.
Under the `Tasks` menu, you can invoke some of the gradle tasks.
From the Debug area, you can select a module/target and debug it if it builds.

# Cross Platform Usage

## Linux and macOS

I have a physical Mac and I'm using [a Docker container][aDockerContainer] for Linux testing.
These are the POSIX systems I'm currently working with.

I've found that I can;
- checkout a working copy onto the POSIX system
- mount the POSIX folder using [a Windows SSHFS tool](https://www.nsoftware.com/netdrive/sftp/)
- use my Windows-based editor(s) to edit the program
- run builds (and get errors) from an SSH shell

I've tried more elegant/elaborate approaches but [certain problems obstruct the approach I'd prefer to use.](https://stackoverflow.com/questions/51623723/)

[aDockerContainer]:
  https://gist.github.com/g-pechorin/733c270946d950ddba0ddfcf8fa550e4

