package com.tu.courier.dao;

import com.tu.courier.dto.EnumCountRow;
import com.tu.courier.dto.OfficeReportRow;
import com.tu.courier.util.HibernateUtil;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // Ако искаш да ги показваш като Double в UI (най-сигурно при агрегати)
    public record Summary(long totalCount, Double totalRevenue, Double avgPrice, Double avgWeight) {}

    public Summary getSummary(LocalDateTime from, LocalDateTime to) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Object[] row = session.createQuery("""
                select
                  count(s.id),
                  coalesce(sum(s.price), 0),
                  coalesce(avg(s.price), 0),
                  coalesce(avg(s.weight), 0)
                from Shipment s
                where s.shipmentDate between :from and :to
            """, Object[].class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .uniqueResult();

            long count = (row[0] == null) ? 0L : (Long) row[0];

            Double totalRevenue = toDouble(row[1]);
            Double avgPrice = toDouble(row[2]);
            Double avgWeight = toDouble(row[3]);

            return new Summary(count, totalRevenue, avgPrice, avgWeight);
        }
    }

    public List<OfficeReportRow> getByOffice(LocalDateTime from, LocalDateTime to) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            // ВАЖНО: join s.toOffice (field name), НЕ to_office_id (column name)
            List<Object[]> rows = session.createQuery("""
                select 
                  o.name,
                  count(s.id),
                  coalesce(sum(s.price), 0)
                from Shipment s
                join s.toOffice o
                where s.shipmentDate between :from and :to
                group by o.name
                order by count(s.id) desc
            """, Object[].class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .list();

            List<OfficeReportRow> result = new ArrayList<>();
            for (Object[] r : rows) {
                result.add(new OfficeReportRow(
                        (String) r[0],
                        (Long) r[1],
                        toDouble(r[2]) // price може да се върне като Double/BigDecimal -> нормализираме
                ));
            }
            return result;
        }
    }

    public List<EnumCountRow> getCountByStatus(LocalDateTime from, LocalDateTime to) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            List<Object[]> rows = session.createQuery("""
                select s.status, count(s.id)
                from Shipment s
                where s.shipmentDate between :from and :to
                group by s.status
                order by count(s.id) desc
            """, Object[].class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .list();

            List<EnumCountRow> result = new ArrayList<>();
            for (Object[] r : rows) {
                result.add(new EnumCountRow(String.valueOf(r[0]), (Long) r[1]));
            }
            return result;
        }
    }

    public List<EnumCountRow> getCountByType(LocalDateTime from, LocalDateTime to) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            List<Object[]> rows = session.createQuery("""
                select s.shipmentType, count(s.id)
                from Shipment s
                where s.shipmentDate between :from and :to
                group by s.shipmentType
                order by count(s.id) desc
            """, Object[].class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .list();

            List<EnumCountRow> result = new ArrayList<>();
            for (Object[] r : rows) {
                result.add(new EnumCountRow(String.valueOf(r[0]), (Long) r[1]));
            }
            return result;
        }
    }

    // ---------- helper: нормализира Number/BigDecimal/и т.н. към Double ----------
    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double d) return d;
        if (value instanceof Float f) return f.doubleValue();
        if (value instanceof Long l) return l.doubleValue();
        if (value instanceof Integer i) return i.doubleValue();
        if (value instanceof java.math.BigDecimal bd) return bd.doubleValue();
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(value.toString());
    }
}
