# Contribution Guidelines

**Note:** If these contribution guidelines are not followed your issue or PR might be closed, so please read these instructions carefully.


## Contribution types


### Bug Reports

- If you find a bug, please first report it using [Github issues](https://github.com/faire/yawn)
    - First, check if there is not already an issue for it; duplicated issues will be closed.
    - In particular, make sure you have a raw-Hibernate equivalent of what you are trying to accomplish; Yawn is just a layer on top of Hibernate at the end of
      the day.


### Bug Fix

- If you'd like to submit a fix for a bug, please read the [How To](#how-to-contribute) for how to send a Pull Request.
- Indicate on the open issue that you are working on fixing the bug and the issue will be assigned to you.
- Write `Fixes #xxxx` in your PR text, where xxxx is the issue number (if there is one).
- Include a test that isolates the bug and verifies that it was fixed.


### New Features

- If you'd like to add a feature to the library that doesn't already exist, feel free to describe the feature in a new
  [GitHub issue](https://github.com/faire/yawn/issues).
- If you'd like to implement the new feature, please wait for feedback from the project maintainers before spending too much time writing the code.
  In some cases, enhancements may not align well with the project future development direction.
- Implement the code for the new feature and please read the [How To](#how-to-contribute).


### Documentation & Miscellaneous

- If you have suggestions for improvements to the documentation or examples (or something else), we would love to hear about it.
- As always first file a [Github issue](https://github.com/faire/yawn/issues).
- Implement the changes to the documentation, please read the [How To](#how-to-contribute).


## How To Contribute


### Requirements

For a contribution to be accepted:

- Follow the the style and formatting of the project when writing the code;
- Compile, test, format, and lint code using `./gradlew build` (or your IDE of choice);
- Documentation should always be updated or added (if applicable);
- Tests should always be updated or added (if applicable) -- check the [Test writing guide] for more details;
- The PR title should start with a [conventional commit] prefix (`feat:`, `fix:` etc).

If the contribution doesn't meet these criteria, a maintainer will discuss it with you on the issue or PR. You can still continue to add more commits to the
branch you have sent the Pull Request from and it will be automatically reflected in the PR.


## Open an issue and fork the repository

- If it is a bigger change or a new feature, first of all [file a bug or feature report](https://github.com/faire/yawn/issues), so that we can discuss what
  direction to follow.
- [Fork the project](https://docs.github.com/en/get-started/quickstart/contributing-to-projects) on GitHub.
- Clone the forked repository to your local development machine (e.g. `git clone git@github.com:<YOUR_GITHUB_USER>/yawn.git`).


### Environment Setup

Yawn is managed with the included Gradle wrapper, which should download the appropriate version for you. You will also need Java `21` or newer.

To run the "build", which includes compilation, tests, lint, formatting, and other checks, run:


```shell
./gradlew build
```


#### CSpell

If you want to run the spellchecker locally, you will have to install [cspell](https://github.com/streetsidesoftware/cspell/tree/main/packages/cspell); you can
do so using npm or yarn:

```bash
npm install -g cspell
```

Then you can run it with the following arguments:

```bash
./scripts/cspell-run.sh
```


#### Markdown Lint

If you want to lint the markdown files you have to install [markdownlint-cli](https://github.com/igorshubovych/markdownlint-cli); once that is installed you can
run `scripts/markdownlint-run.sh` to check if the markdown follows the rules.

Note that, sadly, a particularly laborious rule, MD013, [does not provide an auto-fix option](https://github.com/DavidAnson/markdownlint/issues/535). However,
you can use other tools to circumvent this. For example, the extension [Rewrap](https://stkb.github.io/Rewrap/) for VSCode, when
[configured with](https://stkb.github.io/Rewrap/configuration/) `rewrap.wrappingColumn=160`, will do the trick for you.


### Performing changes

- Create a new local branch from `main` (e.g. `git checkout -b my-new-feature`)
- Make your changes (try to split them up with one PR per feature/fix).
- When committing your changes, make sure that each commit message is clear
- Push your new branch to your own fork into the same remote branch


### Breaking changes

When doing breaking changes, a deprecation tag should be added first containing a message that conveys to what method should be used instead to perform the
task.

Also don't forget to the include the `!` as part of your conventional commit prefix when actually removing old code.


### Open a pull request

Go to the [pull request page of Yawn](https://github.com/faire/yawn/pulls) and in the top of the page it will ask you if you want to open a pull request from
your newly created branch.

The title of the pull request should start with a [conventional commit](https://www.conventionalcommits.org/en/v1.0.0/) type.

Allowed types are:

- `fix:` -- patches a bug and is not a new feature;
- `feat:` -- introduces a new feature;
- `docs:` -- updates or adds documentation or examples;
- `test:` -- updates or adds tests;
- `refactor:` -- refactors code but doesn't introduce any changes or additions to the public API;
- `perf:` -- code change that improves performance;
- `build:` -- code change that affects the build system or external dependencies;
- `ci:` -- changes to the CI configuration files and scripts;
- `chore:` -- other changes that don't modify source or test files;
- `revert:` -- reverts a previous commit.

If you introduce a **breaking change** the conventional commit type MUST end with an exclamation mark (e.g. `feat!: Remove method XXX`).

The sentence of the commit (after the `:`) should start with a verb in the present tense; as an example, think that the commit message will complete the
sentence "This commit will ...". For example, "Add support for ..." or "Fix bug with ...".
