package org.opensearch.dataprepper.plugins.processor.registrar;

import org.opensearch.dataprepper.plugins.processor.provider.rules.RuleProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RuleProviderRegistrar {
    private final Map<String, Supplier<RuleProvider>> ruleProviderSuppliers;

    public RuleProviderRegistrar() {
        ruleProviderSuppliers = new HashMap<>();
    }

    public void registerRuleProvider(final String ruleLocation, final Supplier<RuleProvider> ruleProviderSupplier) {
        ruleProviderSuppliers.put(ruleLocation, ruleProviderSupplier);
    }

    public RuleProvider getRuleProvider(final String ruleLocation) {
        final Supplier<RuleProvider> ruleProviderSupplier = ruleProviderSuppliers.get(ruleLocation);
        if (ruleProviderSupplier == null) {
            throw new IllegalArgumentException("No RuleProvider registered for location: " + ruleLocation);
        }

        return ruleProviderSupplier.get();
    }
}
