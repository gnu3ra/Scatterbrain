package net.ballmerlabs.scatterbrain.datastore;

/**
 * Basic data unit for a single message. 
 */
public class Message {



    public String subject;
    public String contents;
    public int ttl;
    public String replyto;
    public String uuid;
    public String recipient;
    public String from;
    public String flags;
    public String sig;

    public Message(String subject, String contents, int ttl, String replyto, String uuid,
                   String recipient, String from, String flags,  String sig) {
        this.subject = subject;
        this.contents = contents;
        this.ttl = ttl;
        this.replyto = replyto;
        this.uuid = uuid;
        this.recipient = recipient;
        this.from = from;
        this.flags = flags;
        this.sig = sig;
    }


}