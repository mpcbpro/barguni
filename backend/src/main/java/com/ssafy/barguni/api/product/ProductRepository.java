package com.ssafy.barguni.api.product;

import com.ssafy.barguni.api.Picture.Picture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);
    Boolean existsProductByBarcode(String barcode);
    Boolean existsProductByPicture(Picture picture);
}
