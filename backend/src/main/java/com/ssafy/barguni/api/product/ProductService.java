package com.ssafy.barguni.api.product;

import com.ssafy.barguni.api.Picture.Picture;
import com.ssafy.barguni.api.Picture.PictureEntity;
import com.ssafy.barguni.api.error.ErrorCode;
import com.ssafy.barguni.api.error.ErrorResVO;
import com.ssafy.barguni.api.error.Exception.ProductException;
import com.ssafy.barguni.common.util.barcodeSearch.BarcodeSearchUtil;
import com.ssafy.barguni.common.util.NaverImgSearchUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ssafy.barguni.api.Picture.PictureService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository prodRepository;
    private final PictureService pictureService;
    private final NaverImgSearchUtil naverImgSearchUtil;
    private final BarcodeSearchUtil barcodeSearchUtil;

    public Product register(String barcode) throws Exception {
        if (prodRepository.existsProductByBarcode(barcode)) {
            return prodRepository.findByBarcode(barcode).get();
        } else {
            String name = null;

            try{
                name = barcodeSearchUtil.getProdNameFromC005(barcode);
            } catch (Exception e){
                e.printStackTrace();
            }

            if (name == null) {
                try{
                    name = barcodeSearchUtil.getProdNameFromI2570(barcode);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            if (name==null) throw new ProductException(new ErrorResVO(ErrorCode.PRODUCT_CODE_NOT_FOUND));

            Picture pic = searchImg(name);
            Product newProd = Product.createProduct(pic, barcode, name);
            return prodRepository.save(newProd);
        }
    }


    public String searchTest(String word) throws Exception {
        return naverImgSearchUtil.imageSearch(word);
    }


    public Picture searchImg(String prodName) throws Exception {
        // ????????? ????????? ???????????? ????????? url??? ??????
        String imgUrl = naverImgSearchUtil.imageSearch(prodName);
        System.out.println(imgUrl);
        // url??? ?????? ???????????? bufferedImage??? ????????????, ???????????? ?????????
        URL url = new URL(imgUrl);
        URLConnection conn = url.openConnection();
        BufferedImage image = ImageIO.read(conn.getInputStream());
        String imgExtension = conn.getContentType().split("/")[1];

//        ImageUtil.createByBufferedImg(image, "item", word, imgExtension); // multipartFile??? ???????????? ?????? ???????????? ??????
        // ???????????? multipartFile??? ??????
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, imgExtension, baos);
        MultipartFile multipartFile = new MockMultipartFile(prodName, prodName+"."+imgExtension,"image/"+imgExtension, baos.toByteArray());

        // PictureService??? ?????? ????????? ????????? ??????.
        return pictureService.create(multipartFile, PictureEntity.ITEM);
    }

}
