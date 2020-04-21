# mailsender
Stand-alone rest service that sends email

This lightweight service listens for posts and then sends them to the email address specified as a command line argument.
Enables mail-sending from a form without the use of PHP or CGI scripts and without including your mail address in the html source.
To run the service, clone the project and build it:

```
mvn clean install
```

Now run the executable jar with your email address and the redirect page as parameters:

```
java -jar target/mailsender-1.0-SNAPSHOT.jar yourname@example.org https://example.org/thank_you.html
```

After this you will have a service listening for mail-posts on port <yourhost>:4567/mail

The service expects the POST request to contain the fields `name`, `email` and `message`.

No warranty + feel free to use any way you see fit.
