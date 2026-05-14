package com.agentos.evolution.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SkillMarketplace {
    private static final Logger log = LoggerFactory.getLogger(SkillMarketplace.class);
    private final Map<String, MarketListing> listings = new ConcurrentHashMap<>();

    public static class MarketListing {
        private String id;
        private String skillId;
        private String name;
        private String description;
        private String author;
        private String version;
        private String category;
        private double price;
        private int downloads;
        private double rating;
        private int ratingCount;
        private List<String> tags;
        private LocalDateTime publishedAt;

        public MarketListing(String skillId, String name, String description, String author) {
            this.id = UUID.randomUUID().toString();
            this.skillId = skillId;
            this.name = name;
            this.description = description;
            this.author = author;
            this.version = "1.0.0";
            this.category = "general";
            this.tags = new ArrayList<>();
            this.publishedAt = LocalDateTime.now();
        }

        public String getId() { return id; }
        public String getSkillId() { return skillId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getAuthor() { return author; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getDownloads() { return downloads; }
        public void incrementDownloads() { downloads++; }
        public double getRating() { return rating; }
        public int getRatingCount() { return ratingCount; }
        public void addRating(double r) {
            rating = (rating * ratingCount + r) / (ratingCount + 1);
            ratingCount++;
        }
        public List<String> getTags() { return tags; }
        public void addTag(String tag) { tags.add(tag); }
    }

    public MarketListing publish(String skillId, String name, String description, String author) {
        MarketListing listing = new MarketListing(skillId, name, description, author);
        listings.put(listing.getId(), listing);
        log.info("Published skill to marketplace: {} by {}", name, author);
        return listing;
    }

    public List<MarketListing> search(String query) {
        String q = query.toLowerCase();
        return listings.values().stream()
            .filter(l -> l.getName().toLowerCase().contains(q)
                || l.getDescription().toLowerCase().contains(q)
                || l.getTags().stream().anyMatch(t -> t.toLowerCase().contains(q)))
            .collect(Collectors.toList());
    }

    public List<MarketListing> listByCategory(String category) {
        return listings.values().stream()
            .filter(l -> l.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }

    public List<MarketListing> getTrending() {
        return listings.values().stream()
            .sorted((a, b) -> Integer.compare(b.getDownloads(), a.getDownloads()))
            .limit(10)
            .collect(Collectors.toList());
    }

    public List<MarketListing> getTopRated() {
        return listings.values().stream()
            .filter(l -> l.getRatingCount() >= 3)
            .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
            .limit(10)
            .collect(Collectors.toList());
    }

    public MarketListing getListing(String id) { return listings.get(id); }

    public void recordDownload(String listingId) {
        MarketListing listing = listings.get(listingId);
        if (listing != null) listing.incrementDownloads();
    }

    public void rate(String listingId, double rating) {
        MarketListing listing = listings.get(listingId);
        if (listing != null) listing.addRating(rating);
    }

    public Map<String, Object> getStats() {
        return Map.of(
            "total_listings", listings.size(),
            "total_downloads", listings.values().stream().mapToInt(MarketListing::getDownloads).sum(),
            "categories", listings.values().stream()
                .collect(Collectors.groupingBy(MarketListing::getCategory, Collectors.counting()))
        );
    }
}