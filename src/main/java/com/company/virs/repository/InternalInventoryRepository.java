package com.company.virs.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Repository
@RequiredArgsConstructor
public class InternalInventoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

        public Map<String, Integer> findQuantitiesByProductCode(
                List<String> productCodes) {

            if (productCodes == null || productCodes.isEmpty()) {
                return Collections.emptyMap();
            }

            String sql = """
                    SELECT product_code, quantity
                    FROM internal_inventory
                    WHERE product_code IN (:productCodes)
                    """;

            MapSqlParameterSource parameters =
                    new MapSqlParameterSource()
                            .addValue("productCodes", productCodes);

            return jdbcTemplate.query(
                    sql,
                    parameters,
                    resultSet -> {

                        Map<String, Integer> result =
                                new HashMap<>();

                        while (resultSet.next()) {

                            result.put(
                                    resultSet.getString("product_code"),
                                    resultSet.getInt("quantity")
                            );
                        }

                        return result;
                    }
            );
        }
}