package org.opensearch.dataprepper.plugins.processor.registrar;

import org.opensearch.dataprepper.plugins.processor.retrievers.SubMatchAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SubMatchAccessorRegistrar {
    private final Map<String, Supplier<SubMatchAccessor>> subMatchAccessorSuppliers;

    public SubMatchAccessorRegistrar() {
        subMatchAccessorSuppliers = new HashMap<>();
    }

    public void registerSubMatchAccessor(final String accessorName, final Supplier<SubMatchAccessor> subMatchAccessorSupplier) {
        subMatchAccessorSuppliers.put(accessorName, subMatchAccessorSupplier);
    }

    public SubMatchAccessor getSubMatchAccessor(final String accessorName) {
        final Supplier<SubMatchAccessor> subMatchAccessorSupplier = subMatchAccessorSuppliers.get(accessorName);
        if (subMatchAccessorSupplier == null) {
            throw new IllegalArgumentException("No SubMatchAccessor registered for name: " + accessorName);
        }

        return subMatchAccessorSupplier.get();
    }
}
