import com.tjpu.auth.AuthSystemApplication;
import com.tjpu.auth.common.utils.SpringContextHolderUtil;
import com.tjpu.pk.common.utils.ApiDocumentGeneratorUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author: chengzq
 * @date: 2018/8/7 0007 13:58
 * @Description:  使用该测试类生成api文档
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthSystemApplication.class)
@WebAppConfiguration
public class ApiGenerate {
    @Autowired
    private SpringContextHolderUtil SpringContextHolderUtil;

    @Test
    public void getDocumentApi() throws Exception {

        String filePath="D:\\";
        String fileName="ApiDocument.html";
        ApiDocumentGeneratorUtil.apiDocumentGenerat(SpringContextHolderUtil,filePath,fileName);
        //ApiDocumentGeneratorUtil.apiDocumentGenerat(new SpringContextHolderUtil());
    }

}
