package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.repository.ChildRepository;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Matches a name typed into the public form against the Child master list, tolerating:
 *  - minor spelling mistakes (typo-tolerant similarity, not exact string equality)
 *  - "bhai" / "ben" suffixes some kids add or omit (e.g. "Meet bhai Shah" vs "Meet Shah")
 *  - a middle name that's actually the father's or mother's first name
 *    (e.g. "Meet Kamlesh Shah" where "Kamlesh" is the father's name)
 *
 * The whole roster is loaded into memory for this - fine at the scale of a youth
 * organization's kid roster (hundreds, not millions), and necessary since none of
 * this tolerance can be expressed as a SQL WHERE clause.
 */
@Service
public class NameMatchingService {

    // Below this similarity score, we don't consider it a match at all.
    private static final double MATCH_THRESHOLD = 0.85;

    private static final Pattern HONORIFIC_SUFFIX =
            Pattern.compile("\\b(bhai|ben|bhen)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private final ChildRepository childRepository;
    private final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    public NameMatchingService(ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    public Optional<Child> findBestMatch(String rawEnteredName) {
        String normalizedInput = normalize(rawEnteredName);
        if (normalizedInput.isBlank()) {
            return Optional.empty();
        }

        List<Child> allChildren = childRepository.findAll();

        Child bestMatch = null;
        double bestScore = 0;

        for (Child child : allChildren) {
            double score = scoreCandidate(normalizedInput, child);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = child;
            }
        }

        if (bestMatch != null && bestScore >= MATCH_THRESHOLD) {
            return Optional.of(bestMatch);
        }
        return Optional.empty();
    }

    /**
     * Tries a couple of variants of the entered name against this specific child's
     * master-list name, and returns the best (highest) similarity across them:
     *   1. entered name as-is vs firstName + lastName
     *   2. entered name with any token matching the father's/mother's first name removed
     *      (handles the "name entered with a middle name" case) vs firstName + lastName
     */
    private double scoreCandidate(String normalizedInput, Child child) {
        String masterName = normalize(child.getFirstName() + " " + child.getLastName());

        double directScore = similarity.apply(normalizedInput, masterName);

        String withoutParentToken = removeParentNameToken(normalizedInput, child);
        double strippedScore = withoutParentToken.equals(normalizedInput)
                ? 0
                : similarity.apply(withoutParentToken, masterName);

        return Math.max(directScore, strippedScore);
    }

    private String removeParentNameToken(String normalizedInput, Child child) {
        String fatherFirstToken = firstToken(child.getFatherName());
        String motherFirstToken = firstToken(child.getMotherName());

        List<String> tokens = new java.util.ArrayList<>(Arrays.asList(normalizedInput.split(" ")));
        tokens.removeIf(token ->
                (fatherFirstToken != null && similarity.apply(token, fatherFirstToken) >= MATCH_THRESHOLD) ||
                (motherFirstToken != null && similarity.apply(token, motherFirstToken) >= MATCH_THRESHOLD));

        return String.join(" ", tokens);
    }

    private String firstToken(String name) {
        if (name == null || name.isBlank()) return null;
        String normalized = normalize(name);
        String[] parts = normalized.split(" ");
        return parts.length > 0 ? parts[0] : null;
    }

    private String normalize(String name) {
        if (name == null) return "";
        String noHonorific = HONORIFIC_SUFFIX.matcher(name).replaceAll(" ");
        String collapsed = MULTI_SPACE.matcher(noHonorific.trim()).replaceAll(" ");
        return collapsed.toLowerCase().trim();
    }
}
