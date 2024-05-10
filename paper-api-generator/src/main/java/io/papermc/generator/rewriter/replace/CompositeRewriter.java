package io.papermc.generator.rewriter.replace;

import com.google.common.base.Preconditions;
import io.papermc.generator.rewriter.ClassNamed;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompositeRewriter extends SearchReplaceRewriter {

    private final Map<String, SearchReplaceRewriter> rewriterByPattern;

    private CompositeRewriter(ClassNamed rewriteClass, List<SearchReplaceRewriter> rewriters) {
        super(rewriteClass, null, false);
        this.rewriterByPattern = rewriters.stream().collect(Collectors.toMap(rewriter -> rewriter.pattern, rewriter -> rewriter));
    }

    @Override
    protected void beginSearch() {
        for (SearchReplaceRewriter rewriter : this.getRewriters()) {
            rewriter.beginSearch();
        }
    }

    @Override
    public boolean isVersionDependant() {
        for (SearchReplaceRewriter rewriter : this.getRewriters()) {
            if (rewriter.isVersionDependant()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dump(StringBuilder content) {
        for (SearchReplaceRewriter rewriter : this.getRewriters()) {
            rewriter.dump(content);
        }
    }

    @Override
    protected SearchReplaceRewriter getRewriterFor(String pattern) {
        return this.rewriterByPattern.get(pattern);
    }

    @Override
    public Set<String> getPatterns() {
        return Collections.unmodifiableSet(this.rewriterByPattern.keySet());
    }

    public Collection<SearchReplaceRewriter> getRewriters() {
        return Collections.unmodifiableCollection(this.rewriterByPattern.values());
    }

    public static CompositeRewriter bind(SearchReplaceRewriter... rewriters) {
        return bind(Arrays.asList(rewriters));
    }

    public static CompositeRewriter bind(List<SearchReplaceRewriter> rewriters) {
        Preconditions.checkArgument(!rewriters.isEmpty(), "Rewriter list cannot be empty!");
        ClassNamed rewriteClass = rewriters.get(0).rewriteClass;
        String rootClassName = rewriteClass.root().simpleName();

        for (SearchReplaceRewriter rewriter : rewriters) {
            Preconditions.checkState(!(rewriter instanceof CompositeRewriter), "Nested composite rewriters are not allowed!");
            Preconditions.checkArgument(rewriter.pattern != null, "Rewriter pattern cannot be null!");
            Preconditions.checkState(rewriteClass.packageName().equals(rewriter.rewriteClass.packageName()) &&
                rootClassName.equals(rewriter.rewriteClass.root().simpleName()), "Composite rewriter only works for one file!");
        }

        return new CompositeRewriter(rewriteClass, rewriters);
    }
}
