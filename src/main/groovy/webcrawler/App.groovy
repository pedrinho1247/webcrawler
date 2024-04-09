import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup

import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class App {
    static void main(String[] args) {
        String url = "https://www.gov.br/ans/pt-br/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss/marco-2024"
        String caminho = "/home/pedro/Downloads/PadroTISS_ComponenteOrganizacional_202403.pdf"
        crawl(url, caminho)
    }

    private static void crawl(String url, String caminho) {
        CloseableHttpClient httpclient = HttpClients.createDefault()

        try {
            HttpGet httpGet = new HttpGet(url)
            CloseableHttpResponse response = httpclient.execute(httpGet)

            println("Status da requisição: ${response.getStatusLine().getStatusCode()}")

            if (response.getStatusLine().getStatusCode() == 200) {
                def html = EntityUtils.toString(response.getEntity())
                def doc = Jsoup.parse(html)
                def tbody = doc.select("tbody").first()
                if (tbody != null) {
                    def firstRow = tbody.select("tr").first()
                    if (firstRow != null) {
                        def tdWithHref = firstRow.select("td a[href]").first()
                        if (tdWithHref != null) {
                            def hrefValue = tdWithHref.attr("href")
                            println("Valor do href td: $hrefValue")
                            // Verifica se o diretório de destino existe
                            def diretorioDestino = Paths.get(caminho).getParent()
                            if (!Files.exists(diretorioDestino)) {
                                Files.createDirectories(diretorioDestino)
                            }
                            // Baixa o PDF referenciado pelo href
                            def pdfUrl = new URL(hrefValue)
                            Files.copy(pdfUrl.openStream(), Paths.get(caminho), StandardCopyOption.REPLACE_EXISTING)
                            println("PDF baixado com sucesso.")
                        } else {
                            println("Nenhum link encontrado")
                        }
                    } else {
                        println("Nenhuma linha encontrada dentro do tbody.")
                    }
                } else {
                    println("tbody não encontrado nesta página.")
                }
            } else {
                println("Erro ao acessar a página: ${response.getStatusLine().getStatusCode()}")
            }
        } catch (Exception e) {
            println("Erro ao acessar a página: ${e.message}")
            e.printStackTrace()
        } finally {
            httpclient.close()
        }
    }
}
