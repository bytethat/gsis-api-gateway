package gr.bytethat.gsis.registry.core;

import gr.bytethat.gsis.common.core.GreekVatValidator;
import gr.bytethat.gsis.registry.abstractions.BusinessDetails;
import gr.bytethat.gsis.registry.abstractions.GsisRegistry;
import gr.bytethat.gsis.common.abstractions.exception.GsisException;
import gr.bytethat.gsis.common.abstractions.exception.GsisRemoteException;
import gr.bytethat.gsis.registry.core.client.ObjectFactory;
import gr.bytethat.gsis.registry.core.client.RgWsPublic2ResultRtType;
import gr.bytethat.gsis.registry.core.client.RgWsPublic2Service;
import lombok.extern.slf4j.Slf4j;

import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;
import java.util.List;

@Slf4j
public final class GsisRegistryImpl implements GsisRegistry {

    private final GsisRegistryOptions options;
    private final RgWsPublic2Service service;

    public GsisRegistryImpl(GsisRegistryOptions options) {
        this.options = options;

        this.service = new RgWsPublic2Service();
        this.service.setHandlerResolver(_ -> List.of(new GsisSecurityHandler(this.options.username(), this.options.password())));
    }

    @Override
    public BusinessDetails lookup(String vat) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        try {
            // Construct input record using generated ObjectFactory
            var factory = new ObjectFactory();
            var input = factory.createRgWsPublic2InputRtType();

            // Set input values wrapped inside JAXBElements due to nillable/minOccurs fields
            input.setAfmCalledBy(factory.createRgWsPublic2InputRtTypeAfmCalledBy(this.options.calledBy()));
            input.setAfmCalledFor(factory.createRgWsPublic2InputRtTypeAfmCalledFor(vat));

            // Set current date for the request
            var currentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
            input.setAsOnDate(factory.createRgWsPublic2InputRtTypeAsOnDate(currentDate));

            RgWsPublic2ResultRtType response;
            try {
                var result = service.getRgWsPublic2ServicePort().rgWsPublic2AfmMethod(input);

                response = (result != null) ? result.getRgWsPublic2ResultRtType() : null;

                if (response == null) {
                    throw new NullPointerException("GSIS returned null response");
                }
            } catch (Exception e) {
                log.error("GSIS lookup failed", e);

                throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
            }

            var error = response.getErrorRec();
            if (error != null) {
                var code = error.getErrorCode() != null ? error.getErrorCode().getValue() : null;
                var description = error.getErrorDescr() != null ? error.getErrorDescr().getValue() : null;

                if (code != null && !code.trim().isEmpty() && !"OK".equalsIgnoreCase(code.trim())) {
                    throw new GsisRemoteException(code, description);
                }
            }

            return BusinessDetailsMapper.map(response);

        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }
}
