package talentcapitalme.com.comparatio.service;

/**
 * Interface for Matrix Seeder Service operations
 */
public interface IMatrixSeederService {
    
    /**
     * Seed default matrices for a client if none exist
     */
    void seedDefaultsForClient(String clientId);
}
