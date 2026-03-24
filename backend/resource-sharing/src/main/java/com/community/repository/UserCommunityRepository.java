import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.community.entity.UserCommunity;

public interface UserCommunityRepository extends JpaRepository<UserCommunity, Long> {
    List<UserCommunity> findByUserUserId(Long userId); // ✅ Long
}
