package io.papermc.generator.rewriter.context;

public interface ImportCollector {

    ImportCollector NO_OP = new ImportCollector() {
        @Override
        public String getStaticAlias(final String fqn) {
            return fqn;
        }

        @Override
        public String getTypeName(final Class<?> clazz) {
            return clazz.getCanonicalName();
        }

        @Override
        public void consume(final String line) {

        }
    };

    String getStaticAlias(String fqn);

    String getTypeName(Class<?> clazz);

    void consume(String line);

}