package tn.esprit.entities;

public class UserReputation {
    private long reputationId;
    private long userId;
    private int completedContracts;
    private int canceledContracts;
    private int totalScore; // Sum of all star ratings
    private int ratingCount; // Number of times rated

    public UserReputation() {
    }

    public UserReputation(long reputationId, long userId, int completedContracts, int canceledContracts, int totalScore,
            int ratingCount) {
        this.reputationId = reputationId;
        this.userId = userId;
        this.completedContracts = completedContracts;
        this.canceledContracts = canceledContracts;
        this.totalScore = totalScore;
        this.ratingCount = ratingCount;
    }

    public long getReputationId() {
        return reputationId;
    }

    public void setReputationId(long reputationId) {
        this.reputationId = reputationId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getCompletedContracts() {
        return completedContracts;
    }

    public void setCompletedContracts(int completedContracts) {
        this.completedContracts = completedContracts;
    }

    public int getCanceledContracts() {
        return canceledContracts;
    }

    public void setCanceledContracts(int canceledContracts) {
        this.canceledContracts = canceledContracts;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    /**
     * Calculates the average star rating (1 to 5).
     * Defaults to 0.0 if not rated yet.
     */
    public double getAverageRating() {
        if (ratingCount == 0 || totalScore == 0)
            return 0.0;
        return (double) totalScore / ratingCount;
    }
}
