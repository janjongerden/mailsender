# mailsender
Stand-alone rest service that sends email

This light weight service listens for posts and then sends them to the email address specified as a command line argument.
Enables mail-sending from a form without the use of PHP or CGI scripts and without including your mail address in the html source.
To run the service, clone the project and run this maven command:

mvn clean install exec:java -Dexec.mainClass="nl.janjongerden.mail.MailSender" -Dexec.args="yourname@example.org https://example.org/thank_you.html"

After this you will have a service listening for mail-posts on port <yourhost>:4567/mail

No warranty + feel free to use any way you see fit.
