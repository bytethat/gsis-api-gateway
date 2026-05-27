package gr.bytethat.gsis.registry.core;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

final class GsisSecurityHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger log = LoggerFactory.getLogger(GsisSecurityHandler.class);

    private static final String WSSE_PREFIX = "ns1";
    private static final String WSSE_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    private final String username;
    private final String password;

    public GsisSecurityHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Set<QName> getHeaders() {
        var securityHeader = new QName(WSSE_NAMESPACE, "Security", WSSE_PREFIX);
        return Collections.singleton(securityHeader);
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (Boolean.TRUE.equals(outbound)) {
            try {
                SOAPMessage message = context.getMessage();
                SOAPPart soapPart = message.getSOAPPart();
                SOAPEnvelope envelope = soapPart.getEnvelope();
                SOAPHeader header = envelope.getHeader();
                if (header == null) {
                    header = envelope.addHeader();
                }

                // Add Security Element
                SOAPElement security = header.addChildElement("Security", WSSE_PREFIX, WSSE_NAMESPACE);

                // Add UsernameToken Element
                SOAPElement usernameToken = security.addChildElement("UsernameToken", WSSE_PREFIX);

                // Add Username Element
                SOAPElement usernameElem = usernameToken.addChildElement("Username", WSSE_PREFIX);
                usernameElem.addTextNode(this.username);

                // Add Password Element
                SOAPElement passwordElem = usernameToken.addChildElement("Password", WSSE_PREFIX);
                passwordElem.addTextNode(this.password);

                message.saveChanges();
            } catch (Exception e) {
                log.error("Failed to inject WS-Security headers into outbound SOAP request", e);
                throw new RuntimeException("Error injecting WS-Security headers", e);
            }
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
        // No-op
    }
}
