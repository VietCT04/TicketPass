package com.ticketpass.api.settlement.mock;
import com.ticketpass.api.settlement.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
@Service
@ConditionalOnProperty(name = "ticketpass.settlement.provider", havingValue = "mock")
public class MockSettlementProvider implements SettlementProvider {
    private final SettlementProperties properties;
    public MockSettlementProvider(SettlementProperties properties){this.properties=properties;}
    public String providerName(){return "MOCK";}
    public SettlementReleaseResult release(SettlementReleaseRequest request){
        if(!properties.enabled() || !properties.mock().enabled()) return new SettlementReleaseResult(SettlementReleaseResult.Outcome.UNKNOWN, null, "MOCK_DISABLED");
        return result(request);
    }
    public SettlementReleaseResult lookup(SettlementReleaseRequest request){return result(request);}
    private SettlementReleaseResult result(SettlementReleaseRequest request){
        SettlementReleaseResult.Outcome outcome = SettlementReleaseResult.Outcome.valueOf(properties.mock().outcome());
        return new SettlementReleaseResult(outcome, "mock-settlement-" + request.orderId(), outcome == SettlementReleaseResult.Outcome.SUCCEEDED ? null : outcome.name());
    }
}
