package nl.janjongerden.mail;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

            if (sendMail(email, name, message)) {
                response.redirect(thanksLocation);
            }

            return "Something went wrong:( Your message has not been sent.";
        });
    }

    private static boolean sendMail(String fromEmail, String name, String message) {

        message = "Someone with email address '" + fromEmail + "' named '" + name
                + "' sends you this message: \n\n" + message;

        LOG.info("Sending email to " + toAddress + ", message=\n\n" + message);

        try {
            Email email = new SimpleEmail();

            email.setHostName("localhost");
            email.setFrom("mailform@janjongerden.nl");
            if (EmailValidator.getInstance().isValid(fromEmail)) {
                email.addReplyTo(fromEmail);
            }
            email.setSubject("contact form message");
            email.setMsg(message);
            email.addTo(toAddress);
            email.send();
        } catch (EmailException e) {
            LOG.error("Failed to send message: " + e.getMessage(), e);
            return false;
        }

        return true;
    }
}
