package nl.janjongerden.mail;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Start a Spark server at port 4567 that listens for form posts of a contact form.
 * Then sends a message to the email address that was specified as a command line parameter.
 */
public class MailSender {

    private static String toAddress = "someone@example.org";

    private static String thanksLocation = "/thanks";

    private static final Logger LOG = LogManager.getLogger(MailSender.class);


    /**
     * Start the server.
     *
     * @param args optional parameters, first argument is parsed as the 'to' address the
     *             mails will be sent to, second argument is the location of the thankyou-page
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            toAddress = args[0];
        }
        if (args.length > 1) {
            thanksLocation = args[1];
        }
        LOG.info("Mailsender started with toaddress '"+toAddress+"' and thanks page '"+thanksLocation+"'");

        get("/thanks", (request, response) -> "Thank you for your message!");

        post("/mail", (request, response) -> {
            String name = request.queryParams("name");
            String email = request.queryParams("email");
            String message = request.queryParams("message");
            // this is a field only intended to be used by spam bots
            String noHuman = request.queryParams("nohuman");

            if (sendMail(email, name, message, noHuman)) {
                response.redirect(thanksLocation);
            }

            return "Something went wrong:( Your message has not been sent.";
        });
    }

    private static boolean sendMail(String fromEmail, String name, String message, String noHuman) {

        String body = "Someone with email address '" + fromEmail + "' named '" + name
                + "' sends you this message: \n\n" + message;

        if (Strings.isNotEmpty(noHuman)) {
            body = "ROBOT ALERT!! The robot says: '" + noHuman + "'\n\n" + body;
        }

        LOG.info("Sending email to " + toAddress + ", message=\n\n" + body);

        try {
            Email email = new SimpleEmail();

            email.setHostName("localhost");
            email.setFrom("mailform@janjongerden.nl");
            if (EmailValidator.getInstance().isValid(fromEmail)) {
                email.addReplyTo(fromEmail);
            }
            email.setSubject("contact form message");
            email.setMsg(body);
            email.addTo(toAddress);
            email.send();
        } catch (EmailException e) {
            LOG.error("Failed to send message: " + e.getMessage(), e);
            return false;
        }

        return true;
    }
}
