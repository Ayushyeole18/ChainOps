package com.supplychainx.ai.dao;

import com.supplychainx.ai.model.DemandForecast;
import com.supplychainx.ai.model.ModelTrainingMetadata;
import com.supplychainx.ai.model.StockRiskPrediction;
import com.supplychainx.ai.model.SupplierDelayPrediction;
import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MlDaoImpl implements MlDao {

    @Override
    public void saveDemandForecast(DemandForecast forecast) {
        String sql = """
            INSERT INTO demand_forecasts
            (product_id, forecast_period_days, historical_daily_avg, predicted_quantity,
             confidence_lower, confidence_upper, trend_direction, trend_slope)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            forecast_period_days = VALUES(forecast_period_days),
            historical_daily_avg = VALUES(historical_daily_avg),
            predicted_quantity = VALUES(predicted_quantity),
            confidence_lower = VALUES(confidence_lower),
            confidence_upper = VALUES(confidence_upper),
            trend_direction = VALUES(trend_direction),
            trend_slope = VALUES(trend_slope),
            calculated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, forecast.getProductId());
            ps.setInt(2, forecast.getForecastPeriodDays());
            ps.setDouble(3, forecast.getHistoricalDailyAvg());
            ps.setInt(4, forecast.getPredictedQuantity());
            ps.setInt(5, forecast.getConfidenceLower());
            ps.setInt(6, forecast.getConfidenceUpper());
            ps.setString(7, forecast.getTrendDirection());
            ps.setDouble(8, forecast.getTrendSlope());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save demand forecast: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DemandForecast> getAllDemandForecasts() {
        String sql = """
            SELECT df.*, p.sku, p.product_name, c.category_name
            FROM demand_forecasts df
            JOIN products p ON df.product_id = p.product_id
            JOIN categories c ON p.category_id = c.category_id
            ORDER BY df.predicted_quantity DESC
        """;

        List<DemandForecast> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapForecast(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load demand forecasts: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<DemandForecast> getDemandForecastByProductId(int productId) {
        String sql = """
            SELECT df.*, p.sku, p.product_name, c.category_name
            FROM demand_forecasts df
            JOIN products p ON df.product_id = p.product_id
            JOIN categories c ON p.category_id = c.category_id
            WHERE df.product_id = ?
            ORDER BY df.calculated_at DESC LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapForecast(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query product forecast: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private DemandForecast mapForecast(ResultSet rs) throws SQLException {
        DemandForecast df = new DemandForecast();
        df.setForecastId(rs.getInt("forecast_id"));
        df.setProductId(rs.getInt("product_id"));
        df.setSku(rs.getString("sku"));
        df.setProductName(rs.getString("product_name"));
        df.setCategoryName(rs.getString("category_name"));
        df.setForecastPeriodDays(rs.getInt("forecast_period_days"));
        df.setHistoricalDailyAvg(rs.getDouble("historical_daily_avg"));
        df.setPredictedQuantity(rs.getInt("predicted_quantity"));
        df.setConfidenceLower(rs.getInt("confidence_lower"));
        df.setConfidenceUpper(rs.getInt("confidence_upper"));
        df.setTrendDirection(rs.getString("trend_direction"));
        df.setTrendSlope(rs.getDouble("trend_slope"));
        df.setCalculatedAt(rs.getTimestamp("calculated_at"));
        return df;
    }

    @Override
    public void saveStockRiskPrediction(StockRiskPrediction risk) {
        String sql = """
            INSERT INTO stock_risk_predictions
            (product_id, current_stock, daily_velocity, predicted_30d_demand,
             days_until_stockout, risk_level, risk_score, recommended_order_qty)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            current_stock = VALUES(current_stock),
            daily_velocity = VALUES(daily_velocity),
            predicted_30d_demand = VALUES(predicted_30d_demand),
            days_until_stockout = VALUES(days_until_stockout),
            risk_level = VALUES(risk_level),
            risk_score = VALUES(risk_score),
            recommended_order_qty = VALUES(recommended_order_qty),
            evaluated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, risk.getProductId());
            ps.setInt(2, risk.getCurrentStock());
            ps.setDouble(3, risk.getDailyVelocity());
            ps.setInt(4, risk.getPredicted30dDemand());
            ps.setInt(5, risk.getDaysUntilStockout());
            ps.setString(6, risk.getRiskLevel());
            ps.setDouble(7, risk.getRiskScore());
            ps.setInt(8, risk.getRecommendedOrderQty());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save stock risk: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StockRiskPrediction> getAllStockRiskPredictions() {
        String sql = """
            SELECT srp.*, p.sku, p.product_name, p.reorder_level, c.category_name
            FROM stock_risk_predictions srp
            JOIN products p ON srp.product_id = p.product_id
            JOIN categories c ON p.category_id = c.category_id
            ORDER BY srp.risk_score DESC, srp.days_until_stockout ASC
        """;

        List<StockRiskPrediction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StockRiskPrediction p = new StockRiskPrediction();
                p.setRiskId(rs.getInt("risk_id"));
                p.setProductId(rs.getInt("product_id"));
                p.setSku(rs.getString("sku"));
                p.setProductName(rs.getString("product_name"));
                p.setCategoryName(rs.getString("category_name"));
                p.setCurrentStock(rs.getInt("current_stock"));
                p.setReorderLevel(rs.getInt("reorder_level"));
                p.setDailyVelocity(rs.getDouble("daily_velocity"));
                p.setPredicted30dDemand(rs.getInt("predicted_30d_demand"));
                p.setDaysUntilStockout(rs.getInt("days_until_stockout"));
                p.setRiskLevel(rs.getString("risk_level"));
                p.setRiskScore(rs.getDouble("risk_score"));
                p.setRecommendedOrderQty(rs.getInt("recommended_order_qty"));
                p.setEvaluatedAt(rs.getTimestamp("evaluated_at"));
                list.add(p);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load stock risk predictions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void saveSupplierDelayPrediction(SupplierDelayPrediction delay) {
        String sql = """
            INSERT INTO supplier_delay_predictions
            (supplier_id, average_lead_time_days, late_delivery_count, total_orders_evaluated,
             delay_probability, risk_category, reliability_score)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            average_lead_time_days = VALUES(average_lead_time_days),
            late_delivery_count = VALUES(late_delivery_count),
            total_orders_evaluated = VALUES(total_orders_evaluated),
            delay_probability = VALUES(delay_probability),
            risk_category = VALUES(risk_category),
            reliability_score = VALUES(reliability_score),
            evaluated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delay.getSupplierId());
            ps.setDouble(2, delay.getAverageLeadTimeDays());
            ps.setInt(3, delay.getLateDeliveryCount());
            ps.setInt(4, delay.getTotalOrdersEvaluated());
            ps.setDouble(5, delay.getDelayProbability());
            ps.setString(6, delay.getRiskCategory());
            ps.setDouble(7, delay.getReliabilityScore());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save supplier delay prediction: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SupplierDelayPrediction> getAllSupplierDelayPredictions() {
        String sql = """
            SELECT sdp.*, s.supplier_name, s.contact_person, s.email
            FROM supplier_delay_predictions sdp
            JOIN suppliers s ON sdp.supplier_id = s.supplier_id
            ORDER BY sdp.delay_probability DESC
        """;

        List<SupplierDelayPrediction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SupplierDelayPrediction p = new SupplierDelayPrediction();
                p.setDelayId(rs.getInt("delay_id"));
                p.setSupplierId(rs.getInt("supplier_id"));
                p.setSupplierName(rs.getString("supplier_name"));
                p.setContactPerson(rs.getString("contact_person"));
                p.setEmail(rs.getString("email"));
                p.setAverageLeadTimeDays(rs.getDouble("average_lead_time_days"));
                p.setLateDeliveryCount(rs.getInt("late_delivery_count"));
                p.setTotalOrdersEvaluated(rs.getInt("total_orders_evaluated"));
                p.setDelayProbability(rs.getDouble("delay_probability"));
                p.setRiskCategory(rs.getString("risk_category"));
                p.setReliabilityScore(rs.getDouble("reliability_score"));
                p.setEvaluatedAt(rs.getTimestamp("evaluated_at"));
                list.add(p);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load supplier delay predictions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void saveModelMetadata(ModelTrainingMetadata meta) {
        String sql = """
            INSERT INTO ml_model_metadata
            (model_name, model_type, algorithm, training_sample_size, mae, rmse, r_squared, accuracy_score, status, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            training_sample_size = VALUES(training_sample_size),
            mae = VALUES(mae),
            rmse = VALUES(rmse),
            r_squared = VALUES(r_squared),
            accuracy_score = VALUES(accuracy_score),
            last_trained_at = CURRENT_TIMESTAMP,
            status = VALUES(status),
            notes = VALUES(notes)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meta.getModelName());
            ps.setString(2, meta.getModelType());
            ps.setString(3, meta.getAlgorithm());
            ps.setInt(4, meta.getTrainingSampleSize());
            if (meta.getMae() != null) ps.setDouble(5, meta.getMae()); else ps.setNull(5, Types.DECIMAL);
            if (meta.getRmse() != null) ps.setDouble(6, meta.getRmse()); else ps.setNull(6, Types.DECIMAL);
            if (meta.getRSquared() != null) ps.setDouble(7, meta.getRSquared()); else ps.setNull(7, Types.DECIMAL);
            if (meta.getAccuracyScore() != null) ps.setDouble(8, meta.getAccuracyScore()); else ps.setNull(8, Types.DECIMAL);
            ps.setString(9, meta.getStatus());
            ps.setString(10, meta.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save model metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ModelTrainingMetadata> getAllModelMetadata() {
        String sql = "SELECT * FROM ml_model_metadata ORDER BY model_id ASC";
        List<ModelTrainingMetadata> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ModelTrainingMetadata m = new ModelTrainingMetadata();
                m.setModelId(rs.getInt("model_id"));
                m.setModelName(rs.getString("model_name"));
                m.setModelType(rs.getString("model_type"));
                m.setAlgorithm(rs.getString("algorithm"));
                m.setTrainingSampleSize(rs.getInt("training_sample_size"));
                m.setMae(rs.getObject("mae") != null ? rs.getDouble("mae") : null);
                m.setRmse(rs.getObject("rmse") != null ? rs.getDouble("rmse") : null);
                m.setRSquared(rs.getObject("r_squared") != null ? rs.getDouble("r_squared") : null);
                m.setAccuracyScore(rs.getObject("accuracy_score") != null ? rs.getDouble("accuracy_score") : null);
                m.setLastTrainedAt(rs.getTimestamp("last_trained_at"));
                m.setStatus(rs.getString("status"));
                m.setNotes(rs.getString("notes"));
                list.add(m);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load model metadata: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Double> getHistoricalSalesSeries(int productId) {
        String sql = """
            SELECT soi.quantity
            FROM sales_order_items soi
            JOIN sales_orders so ON soi.so_id = so.so_id
            WHERE soi.product_id = ? AND so.status != 'CANCELLED'
            ORDER BY so.order_date ASC
        """;

        List<Double> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getDouble("quantity"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query sales series: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Integer> getSupplierLeadTimes(int supplierId) {
        String sql = """
            SELECT DATEDIFF(COALESCE(actual_date, expected_date), order_date) as lead_days
            FROM purchase_orders
            WHERE supplier_id = ? AND status != 'CANCELLED'
        """;

        List<Integer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int days = Math.max(1, rs.getInt("lead_days"));
                    list.add(days);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query supplier lead times: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int getSupplierDelayedOrderCount(int supplierId) {
        String sql = """
            SELECT COUNT(*) as delayed_cnt
            FROM purchase_orders
            WHERE supplier_id = ? AND actual_date > expected_date
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("delayed_cnt");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query delayed count: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int getProductTotalStock(int productId) {
        String sql = "SELECT COALESCE(SUM(quantity_available), 0) as total_stock FROM inventory WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_stock");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query product stock: " + e.getMessage(), e);
        }
        return 0;
    }
}
