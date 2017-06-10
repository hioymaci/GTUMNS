# GTU MOODLE NOTIFICATION SYSTEM 

System is running on http://www.haliloymaci.com/moodle You can use it by registering.

You can start this app after configure config.txt (it will be created automatically).

You can put this app on a server and run it forever so you can notify 7 day 24 hours.

 You can learn moodle course link IDs by looking course URL on browser.
 Example: CSE102 - C programming course URL is "http://193.140.134.13/moodle/course/view.php?id=73", so your link ID is 73.
 use comma seperate values for multiple course link IDs, example: 73,74,114.
 some course link IDs:
 
 If any error ocurrecd, remove this file completely on same directory that jar file exists. It will be created automatically with default parameters.

## Usage

java -jar moodleNotify.jar [-t] [-d] [-c configFilePath] [-h]

-t : test mode, do not send mail, only print changes to stdout.

-d : do not display checking time.

-c [configFilePath] : set parameters in "configFile" rathen "config.txt".

-h : Display this help message.


# Contributors

Thansk for Hasan Men (https://github.com/hmenn) to inform 