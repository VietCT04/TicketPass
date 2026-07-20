package com.ticketpass.api.settlement.mock;

import com.ticketpass.api.settlement.SettlementProperties;
import com.ticketpass.api.settlement.SettlementProvider;
import com.ticketpass.api.settlement.SettlementReleaseRequest;
import com.ticketpass.api.settlement.SettlementReleaseResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
@Service
@ConditionalOnProperty(name = "ticketpass.settlement.provider", havingValue = "mock")
public class MockSettlementProvider implements SettlementProvider {

    private final SettlementProperties properties;

    public MockSettlementProvider(SettlementProperties properties) {
        this.properties = properties;
    }

    @Override
    public String providerName() {
        return "MOCK";
    }

    @Override
    public SettlementReleaseResult release(SettlementReleaseRequest request) {
        if (!properties.enabled() || !properties.mock().enabled()) {
            return new SettlementReleaseResult(
                    SettlementReleaseResult.Outcome.UNKNOWN,
                    null,
                    "MOCK_DISABLED");
        }
        return result(request);
    }

    @Override
    public SettlementReleaseResult lookup(SettlementReleaseRequest request) {
        return result(request);
    }

    private SettlementReleaseResult result(SettlementReleaseRequest request) {
        SettlementReleaseResult.Outcome outcome = SettlementReleaseResult.Outcome.valueOf(properties.mock().outcome());
        String errorCode = outcome == SettlementReleaseResult.Outcome.SUCCEEDED ? null : outcome.name();
        return new SettlementReleaseResult(outcome, "mock-settlement-" + request.orderId(), errorCode);
    }
}
