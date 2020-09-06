package nl.janjongerden.mail;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Start a Spark server at port 4567 that listens for form posts of a contact form.
 * Then sends a message to the email address that was specified as a command line parameter.
 */
public class MailSender {

    // the hardcoded correct answer to the robot-filtering question in the form
    private static final String THE_CORRECT_ANSWER = "rabbit";

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
        LOG.info("Mailsender started with toaddress '" + toAddress + "' and thanks page '" + thanksLocation + "'");

        get("/thanks", (request, response) -> "Thank you for your message!");

        post("/mail", (request, response) -> {
            Map<String, String> parameters = new HashMap<>();
            request.queryParams()
                    .forEach(param -> parameters.put(param, request.queryParams(param)));

            if (sendMail(parameters)) {
                response.redirect(thanksLocation);
            }

            return "Something went wrong:( Your message has not been sent.";
        });
    }

    private static boolean sendMail(Map<String, String> params) {
        String turingTestAnswer = params.getOrDefault("turingtest", "it's a robot!");
        
        if (!turingTestAnswer.equalsIgnoreCase(THE_CORRECT_ANSWER)) {
            LOG.info("received an incorrect answer: '" + turingTestAnswer + "', instead of '" + THE_CORRECT_ANSWER + "'");
            // act as if all went well to confuse the robot
            return true;
        }
        
        String message = params.getOrDefault("message", "<no message>");
        String fromEmail = params.get("email");

        StringBuilder body = new StringBuilder("Someone sent a contact message:\n");

        String parameterString = params.entrySet().stream()
                .filter(e -> !"message".equalsIgnoreCase(e.getKey()))
                .map(entry -> String.format("Parameter '%s' => '%s'", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));

        body.append(parameterString);
        body.append ("And the message is: \n\n");
        body.append (message);

        LOG.info("Sending email to " + toAddress + ", message=\n\n" + body);

        try {
            Email email = new SimpleEmail();

            email.setHostName("localhost");
            email.setFrom("mailform" + "@" + "janjongerden.nl");
            if (EmailValidator.getInstance().isValid(fromEmail)) {
                email.addReplyTo(fromEmail);
            }
            email.setSubject("contact form message");
            email.setMsg(body.toString());
            email.addTo(toAddress);
            email.send();
        } catch (EmailException e) {
            LOG.error("Failed to send message: " + e.getMessage(), e);
            return false;
        }

        return true;
    }
}
