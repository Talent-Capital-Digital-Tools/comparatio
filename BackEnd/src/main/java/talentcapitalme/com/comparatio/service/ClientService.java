package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.entity.Client;
import talentcapitalme.com.comparatio.exception.NotFoundException;
import talentcapitalme.com.comparatio.exception.ValidationException;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.ClientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AdjustmentMatrixRepository matrixRepository;
    private final SeedService seedService;

    /**
     * Get all clients
     */
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    /**
     * Get client by ID
     */
    public Client getClientById(String id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
    }

    /**
     * Create a new client and seed default matrices
     */
    public Client createClient(Client client) {
        // Validate unique name
        if (clientRepository.existsByName(client.getName())) {
            throw new ValidationException("Client with name '" + client.getName() + "' already exists");
        }

        // Create client
        client.setActive(true); // New clients are active by default
        Client savedClient = clientRepository.save(client);

        // Create default matrices for the new client
        seedService.seedMatricesForClient(savedClient.getId());

        return savedClient;
    }

    /**
     * Update existing client
     */
    public Client updateClient(String id, Client clientUpdate) {
        Client existingClient = getClientById(id);

        // Update fields
        if (clientUpdate.getName() != null) {
            // Check name uniqueness if changing
            if (!clientUpdate.getName().equals(existingClient.getName()) && 
                clientRepository.existsByName(clientUpdate.getName())) {
                throw new ValidationException("Client with name '" + clientUpdate.getName() + "' already exists");
            }
            existingClient.setName(clientUpdate.getName());
        }
        
        if (clientUpdate.getActive() != null) {
            existingClient.setActive(clientUpdate.getActive());
        }

        return clientRepository.save(existingClient);
    }

    /**
     * Delete client and all associated data
     */
    public void deleteClient(String id) {
        Client client = getClientById(id);
        
        // Delete all matrices for this client
        matrixRepository.deleteByClientId(id);
        
        // Delete the client
        clientRepository.deleteById(id);
    }

    /**
     * Activate client
     */
    public Client activateClient(String id) {
        Client client = getClientById(id);
        client.setActive(true);
        return clientRepository.save(client);
    }

    /**
     * Deactivate client
     */
    public Client deactivateClient(String id) {
        Client client = getClientById(id);
        client.setActive(false);
        return clientRepository.save(client);
    }

    /**
     * Get active clients only
     */
    public List<Client> getActiveClients() {
        return clientRepository.findByActiveTrue();
    }
}