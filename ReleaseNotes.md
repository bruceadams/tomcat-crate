# Release notes

## tomcat-0.6.1

- Relax check on call of tomcat/server as crate function
  The check was triggered by calling server with no direct options

- More crate updates for pallet 0.6.3

- Add missing namespace to tomcat crate tests

- Update crates for pallet 0.6.3

- Add explicit deploy-dir to tomcat crate

- Enable use of multiple tomcat instances

## tomcat-0.6.0

- Add scm information to enable module release

- Fix automatic call of settings if options passed to install

- Update version to 0.6.0-SNAPSHOT
  Add dependency on pallet 0.6.1

- Update tomcat for separate settings-map

- Add a settings phase for the Tomcat crate.
  Adding new function settings to load the session with the values that
  install normally does. Install function now calls settings at the
  beginning to load the settings. Tests pass.


## pallet-crates-0.5.0


## pallet-crates-0.4.4

- Update for repository management in separate namespaces


## pallet-crates-0.4.3

- Fix tomcat snapshot version

- Update versions, and add pallet-crates-test dependencies

- Update snapshot versions

- Update java, maven and tomcat to use pallet 0.4.15

- Fix tomcat home directory to be owned by tomcat
  The jpackage tomcat package seems to leave the tomcat user home owned by
  root:root.

- Update java, tomcat, and maven to use jpackage-utils-compat
  Update java based crates to use the updated jpackage functions in
  pallet.resource.package, based on the jpackage-utils-compat rpm

- Fix the script that checks the tomcat install directory exists

- Update java and tomcat crates for jpackage repos disabled by default

- Update for 0.5.0-SNAPSHOT
  Change pallet.resource.* to pallet.action.*. Change stevedore calls to
  script functions to use unquote and the pallet.script.lib namespace.
  Change request to session.  Change build-resources to build-actions.


## pallet-crates-0.4.2


## pallet-crates-0.4.1


## pallet-crates-0.4.0


## pallet-crates-0.4.0-beta-1

- Update tomcat crate to allow overide of paths, and to allow service control
  to be conditional on configuration changes. Add live test for Ubuntut
  10.04, 10.10 and CentOS 5.3, 5.5.

- Improve documentation of tomcat/server-configuration and tomcat/sever. Add
  attempt to detect use of tomcat/server as a crate function

- Add explicit version option to pallet.crate.tomcat/tomcat

- Updated tomcat documentation

- Fixes for hudson example on amzn-linux

- Move node creation for tests in test-utils. Fix direct java style access to
  nodes from crates

- seperated tests requiring jclouds

- working node-list compute service

- switch tomcat users to be collec rather than aggregate

- Fix tomcat and java crates for centos (and hopefully amazon-linux)

- Fixed remote-file/all-options. Added blobstore deploy test to tomcat crate.

- Added map for distro depenedent package name

- Add retrieval of files from node. Refactor sending of files to remove
  *file-transfers*. Add ssh-key/record-public-key to retrieve a users
  public key from a remote node.

- Switch to explicit namespace qualification

- Update pallets server/ci.  Add some use of hudson-cli to allow triggering
  of builds through pallet.

- remove defresource usage from crate/java

- remove defresource usage from maven, tomcat and hudson crates

- Updated to use template as a map, and for new Hardware in jclouds nodes

- Refactoring to a more functional implementation

- Added package-manager-non-interactive to package/packages

- Made converge run phase by phase, instead of node by node.  Added
  pre-phase. Removed :reload-all from tests to avoid deftype problems.

- Various crate changes. Added Changelog

- Changed pallet.crate.tomcat/deploy arguments

- removed 1.1 compat.  fixed compile errors from id/providerId changes

- changed hudson to pick up tomcat parameters

- Added target/*all-nodes* and target/*target-nodes*.  Made target vars
  uninitialised. Tidied mock.

- Harmonised do-script, cmd-join, et al.  Added defvar, defn, and println to
  stevedore. minor documentation fixes.

- fix typo: jsee => jsse

- Added error handling

- Added tag prefix for converge and lift.  Removed note-type map

- Removed deprecation warnings.  Various fixes.

- clean up of crates for new target bindings

- change pallet.target's *target-template* and *target-tag* to *target-node*
  and *target-node-type*

- Added crontab, cruise-control-rb, and nginx crates.  Added a
  tomcat/undeploy. Added a service/init.

- unification of defresource & defcomponent, elimination of atom usage in
  general, formalized sub-phase / execution type

- added transformations for tomcat Service element; now using namespaced
  keywords for internal configuration of tomcat entities

- refactored pallet.resource/required-resources atom into a thread-bound map

- Added clojure-1.2 profile to pom and fixed a 1.2 issue

- improvements to pallet ci server config

- added ssl connectors for tomcat

- hudson crate has basic functionality

- clojure 1.1 / 1.2 compatibility modifications

- clojure 1.1 / 1.2 compatibility modifications

- Updated tomcat and hudson configuration

- added remote-file option to remote-file for copy files on the remote system

- use pallet.resource.service/service in with-restart

- added tomcat/undeploy and tomcat/undeploy-all

- added support for blanket security policy grants

- add missing template file

- Added tomcat serer.xml. renamed functions in tomcat and hudson in the
  expectation that they are used in a namespace prefixed manner

- added local file deployment function and with-restart macro to tomcat crate

- cleanup (option processing)

- Added defnode, lift and configuration phases. Adds declaritive
  configuration definiton.

- added examples directory. fixes towards functional hudson configuration

- added tomcat-user, hudson-plugin and a couple of test fixes

- fixes for tomcat and hudson

- added crates for sphinx, tomcat and hudson. added a service resource.
  tomact and hudson not yet fully functional.  Fixed escaping in
  mysql-script.

