import dto.NVDDTO;
import utils.APIUtils;

public class Main {
    static void main() {
        APIUtils apiUtils = new APIUtils();
        String apikey = System.getenv("apiKey");
        String json = apiUtils.readAPI(apikey, "nginx");
       // System.out.println(apiUtils.convertFromJson(json, NvdResponseDTO.class));

        String url = "https://services.nvd.nist.gov/rest/json/cves/2.0?keywordSearch=nginx&resultsPerPage=2";
        System.out.println(apiUtils.getWithJacksonGeneric(url, NVDDTO.class));
    }
}
