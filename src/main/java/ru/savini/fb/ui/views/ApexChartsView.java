package ru.savini.fb.ui.views;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.ResponsiveBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.responsive.builder.OptionsBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;


@Route(value = "apexcharts", layout = MainView.class)
@PageTitle("ApexCharts")
public class ApexChartsView extends VerticalLayout {

    public ApexChartsView() {
        add(getApexCharts());
        setWidth("60%");
    }

    private ApexCharts getApexCharts() {
        List<Series<Double>> series = new ArrayList<>();
        series.add(new Series<>("Образование", 11.0));
        series.add(new Series<>("Коты", 2.0));
        series.add(new Series<>("Домашняя еда", 35.0));

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.donut).build())
                .withLegend(LegendBuilder.get()
                        .withPosition(Position.right)
                        .build())
                .withSeries(44.0, 55.0, 41.0, 17.0, 15.0)
//                .withSeries(series.toArray())
                .withResponsive(ResponsiveBuilder.get()
                        .withBreakpoint(480.0)
                        .withOptions(OptionsBuilder.get()
                                .withLegend(LegendBuilder.get()
                                        .withPosition(Position.bottom)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}