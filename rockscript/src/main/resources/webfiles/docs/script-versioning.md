A RockScript server stores a collection of scripts and each script has a collection
of script versions.  Once started, script executions keep running in the same script
version.

A script is uniquely identified by its name. Each time the
<a onclick="show('saveScript')">save script</a> command is executed,
a new script version is created.  A script is automatically created if
no script exists with the given name.

When <a onclick="show('startScript')">starting a script execution</a>, you typically
provide the script name.  That way the script execution is started in the latest version
of the script.