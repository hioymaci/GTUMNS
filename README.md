# GTU MOODLE NOTIFICATION SYSTEM CONFIG FILE 

I created this app because of losing point in course CSE496. Teacher attach duyurular.pdf to course but he/she do not notify students, so
I could not meeting then I lose points.

You can start this app after configure config.txt (it will be created automatically).

You can put this app on a server and run it forever so you can notify 7 day 24 hours.



 You can learn moodle course link IDs by looking course URL on browser.
 Example: CSE102 - C programming course URL is "http://193.140.134.13/moodle/course/view.php?id=73", so your link ID is 73.
 use comma seperate values for multiple course link IDs, example: 73,74,114.
 some course link IDs:
 
 CSE102 - C Programming, linkID: 73
 
 CSE108 - C Programming Laboratory, linkID: 74
 
 CSE244 - Systems Programming, linkID: 119
 
 CSE336 - Microprocessors Laboratory, linkID: 114
 
 CSE422 - Theory of Computation, linkID: 110
 
 CSE425 - Introduction to Operations Research, linkID: 109
 
 BIL436 - Introduction to Digital Integrated Circuits, linkID: 108
 
 CSE444 - Software Engineering II, linkID: 106
 
 CSE445 - Compiler Design, linkID: 105
 
 CSE458 - Introduction to Big Data Analytics, linkID: 102
 
 CSE461 - Computer Graphics, linkID: 101
 
 CSE464 - Digital Image Processing, linkID: 100
 
 CSE495/496 - Bitirme Projesi, linkID: 125
 
 BSB501/MBG524 - Biocomputing, linkID: 127
 
 CSE436/536 - Digital Integrated Circuits, linkID: 92
 
 CSE611 - Big Data Analytics, linkID:

 If any error ocurrecd, remove this file completely on same directory that jar file exists. It will be created automatically with default parameters.

 java -jar moodleNotify.jar [-t] [-d] [-c configFilePath] [-h]
 -t : test mode, do not send mail, only print changes to stdout.
 -d : do not display checking time.
 -c [configFilePath] : set parameters in "configFile" rathen "config.txt".
 -h : Display this help message.