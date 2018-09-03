import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/22
 */
@Slf4j
public class JSONText {

    public static void main(String[] args) {
        JSONObject jo1 = JSONObject.fromObject(new TestEntity("1","车轮滚滚"));

        TestEntity testEntity = (TestEntity) JSONObject.toBean(jo1,TestEntity.class);

        log.info("testEntity:{}",testEntity);
    }
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class TestEntity{
    private String id;
    private String name;

    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
