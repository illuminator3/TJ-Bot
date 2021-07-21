Download latest Java from <https://adoptopenjdk.net/>, run the installer.

Open a CMD and type `java -version` and `javac -version`, if both show your new Java version, you have successfully installed Java! :tada:
https://i.imgur.com/CurMujt.png

In order to prevent issues with old installations you may want to remove them. Type `where java` and `where javac`. They should only show the new and no old entries:
https://i.imgur.com/x5k1GxG.png

If that is not the case, you may want to clean up. Therefore, type "_environment variable_" into the Windows search and follow the dialog. You will see two entries for a variable called `Path`, one for your user and one for the system. This variable tells Windows where to look for commands, like `java` and `javac`.
https://i.imgur.com/3bN9QE2.png

Edit both. Make sure that they only list the entry to your new Java installation and remove all the old entries you have seen with the `where` command before:
https://i.imgur.com/JAYog1U.png

Confirm and close all dialogs. Restart your CMD window and try `where java` and `where javac` again. It should only list the new installation now and nothing else.

Congratulation, your Java is now finally ready, happy coding :tada: