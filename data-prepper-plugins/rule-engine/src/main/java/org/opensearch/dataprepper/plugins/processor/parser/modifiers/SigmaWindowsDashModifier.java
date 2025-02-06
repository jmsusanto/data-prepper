/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.modifiers;

import org.apache.commons.lang3.tuple.Pair;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetectionItem;
import org.opensearch.dataprepper.plugins.processor.parser.types.Placeholder;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaExpansion;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaString;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaType;
import org.opensearch.dataprepper.plugins.processor.parser.utils.AnyOneOf;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SigmaWindowsDashModifier extends SigmaValueModifier {

    public SigmaWindowsDashModifier(SigmaDetectionItem detectionItem, List<Class<? extends SigmaModifier>> appliedModifiers) {
        super(detectionItem, appliedModifiers);
    }

    @Override
    public Pair<Class<?>, Class<?>> getTypeHints() {
        return Pair.of(SigmaString.class, SigmaExpansion.class);
    }

    @Override
    public Either<SigmaType, List<SigmaType>> modify(Either<SigmaType, List<SigmaType>> val) {
        if (val.isLeft() && val.getLeft() instanceof SigmaString) {
            Function<Placeholder, List<AnyOneOf<String, Character, Placeholder>>> callback =
                    p -> {
                        if (p.getName().equals("_windash")) {
                            return List.of(AnyOneOf.leftVal("-"), AnyOneOf.leftVal("/"));
                        }
                        return List.of(AnyOneOf.rightVal(p));
                    };
            return Either.left(new SigmaExpansion(new SigmaString(val.getLeft().toString().replace("_ws_", " ")).replaceWithPlaceholder(Pattern.compile("\\B[-/]\\b"), "_windash")
                    .replacePlaceholders(callback).stream().map(s -> new SigmaString(s.toString().replace(" ", "_ws_"))).collect(Collectors.toList())));
        }
        return null;
    }
}