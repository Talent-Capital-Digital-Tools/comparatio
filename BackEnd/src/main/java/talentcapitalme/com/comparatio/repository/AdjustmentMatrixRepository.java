package talentcapitalme.com.comparatio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface AdjustmentMatrixRepository extends MongoRepository<AdjustmentMatrix, String> {

    /** Find the active adjustment matrix cell for the given performance bucket, compa ratio, and as-of date.
     *
     * @param perfBucket The performance bucket (1-5).
     * @param compa The compa ratio.
     * @param asOf The as-of date to check effectiveness.
     * @return An Optional containing the matching AdjustmentMatrix if found, otherwise empty.
     */


    @Query("{ 'perfBucket': ?0, 'active': true, " +
            " 'compaFrom': { $lte: ?1 }, 'compaTo': { $gt: ?1 }, " +
            " $and: [ { $or: [ { 'effectiveFrom': null }, { 'effectiveFrom': { $lte: ?2 } } ] }, " +
            " { $or: [ { 'effectiveTo': null }, { 'effectiveTo': { $gte: ?2 } } ] } ] }")
    Optional<AdjustmentMatrix> findActiveCell(int perfBucket, BigDecimal compa, LocalDate asOf);


}

