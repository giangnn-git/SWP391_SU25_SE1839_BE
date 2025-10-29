package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SCExpenseResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.SCExpenseReposiotry;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.SCExpenseService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SCExpenseServiceImpl implements SCExpenseService {
        final SCExpenseReposiotry scExpenseReposiotry;

        public double handleCalculateRevenueOfWeek(Long serviceCenterId) {
                LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
                LocalDate endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);

                return scExpenseReposiotry.findByServiceCenterAndEndDateBetween(
                                serviceCenterId,
                                startOfWeek.atStartOfDay(),
                                endOfWeek.plusDays(1).atStartOfDay()).stream()
                                .mapToDouble(se -> se.getAmount() != null ? se.getAmount() : 0)
                                .sum();
        }

        public SCExpenseResponse handleWarrantyCostComparison(Long serviceCenterId) {
                LocalDate startOfThisMonth = LocalDate.now().withDayOfMonth(1);
                LocalDate endOfThisMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
                LocalDate startOfLastMonth = startOfThisMonth.minusMonths(1);
                LocalDate endOfLastMonth = startOfThisMonth.minusDays(1);

                double thisMonth = scExpenseReposiotry.findByServiceCenterAndEndDateBetween(
                                serviceCenterId, startOfThisMonth.atStartOfDay(),
                                endOfThisMonth.plusDays(1).atStartOfDay())
                                .stream()
                                .mapToDouble(se -> se.getAmount() != null ? se.getAmount() : 0)
                                .sum();

                double lastMonth = scExpenseReposiotry.findByServiceCenterAndEndDateBetween(
                                serviceCenterId, startOfLastMonth.atStartOfDay(),
                                endOfLastMonth.plusDays(1).atStartOfDay())
                                .stream()
                                .mapToDouble(se -> se.getAmount() != null ? se.getAmount() : 0)
                                .sum();

                double changePercent = lastMonth == 0 ? 0 : ((thisMonth - lastMonth) / lastMonth) * 100;

                return SCExpenseResponse.builder()
                                .currentCost(thisMonth)
                                .changePercent(changePercent)
                                .build();
        }

}
