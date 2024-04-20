package com.lancer.server;

import com.lancer.entity.TableFiled;
import com.lancer.entity.TableInfo;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DrawServer extends JPanel {
    private static final int frameWidth = 800;
    private static final int frameHeight = 800;
    // 所有的字段列表 椭圆的个数
    private List<TableFiled> tableInfoList;
    private TableInfo tableInfo;
    // 开始点的坐标
    double x = (double) frameWidth / 2;
    double y = (double) frameHeight / 2;
    // 椭圆与开始点的距离
    double len = 300;
    public DrawServer(TableInfo tableInfo, List<TableFiled> tableInfoList) {
        this.tableInfo = tableInfo;
        this.tableInfoList = tableInfoList;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 绘制矩形
        double rectWidth = 150; // 矩形的宽度
        double rectHeight = 90; // 矩形的高度

        Rectangle2D rect = new Rectangle2D.Double(x - rectWidth / 2, y - rectHeight / 2, rectWidth, rectHeight);
        g2d.setColor(Color.BLACK);
        g2d.draw(rect);

        int n = this.tableInfoList.size();

        // 计算每个椭圆之间的角度差
        double angleIncrement = 2 * Math.PI / n;

        // 绘制n个椭圆并连接它们与矩形的边缘
        for (int i = 0; i < n; i++) {
            // 计算当前椭圆的角度
            double angle = i * angleIncrement;

            // 计算当前椭圆的中心坐标

            double centerX = x + len * Math.cos(angle);
            double centerY = y + len * Math.sin(angle);

            // 创建并绘制椭圆
            double ellipseWidth = 135; // 椭圆的宽度
            double ellipseHeight = 80; // 椭圆的高度
            Ellipse2D ellipse = new Ellipse2D.Double(centerX - ellipseWidth / 2, centerY - ellipseHeight / 2, ellipseWidth, ellipseHeight);
            g2d.draw(ellipse);

            // 连接矩形和椭圆的边缘
            double rectConnectorX = x + (rectWidth / 2) * Math.cos(angle);
            double rectConnectorY = y + (rectHeight / 2) * Math.sin(angle);
            // 连线
            g2d.draw(new Line2D.Double(rectConnectorX, rectConnectorY, centerX, centerY));
            // 覆盖掉多余的线段
            Ellipse2D ellipse2 = new Ellipse2D.Double(centerX - ellipseWidth / 2 + 1, centerY - ellipseHeight / 2 + 1, ellipseWidth - 1.5, ellipseHeight - 1.5);
            g2d.setColor(Color.WHITE);
            g2d.fill(ellipse2);
            // 绘制椭圆中的文字
            TableFiled filed = this.tableInfoList.get(i);
            String text = "";
            if (ObjectUtils.isEmpty(filed.getComment())) {
                text = filed.getField();
            } else {
                text = filed.getComment();
            }
            // 在指定位置绘制文字
            g2d.setColor(Color.BLACK); // 设置颜色为白色
            g2d.drawString(text, (int) (centerX - g2d.getFontMetrics().stringWidth(text) / 2), (int) centerY); // 绘制文字
            g2d.setColor(Color.BLACK); // 设置颜色为白色
        }
        // 覆盖矩形多余的线段
        Rectangle2D rect2 = new Rectangle2D.Double(x - rectWidth / 2 + 0.5, y - rectHeight / 2 + 0.5, rectWidth - 0.5, rectHeight - 0.5);
        g2d.setColor(Color.WHITE);
        g2d.fill(rect2);
        // 加入文本
        // 绘制实体的文字"Arial"
        Font font = new Font("宋体", Font.PLAIN, 15);
        g2d.setFont(font);
        String text = "";
        TableInfo info = this.tableInfo;
        if (ObjectUtils.isEmpty(info.getTblComment())) {
            text = info.getTblName();
        } else {
            text = info.getTblComment();
        }
        // 设置颜色为白色
        g2d.setColor(Color.BLACK);
        // 绘制文字
        g2d.drawString(text, (int) (x - g2d.getFontMetrics().stringWidth(text) / 2), (int) y);
        // 设置颜色为白色
        g2d.setColor(Color.BLACK);
    }

    public static JFrame drawChart(TableInfo tableInfo, List<TableFiled> tableInfoList) {
        // 创建并显示绘图窗口
        JFrame frame = new JFrame("一键生成ER图");
        DrawServer ellipses = new DrawServer(tableInfo, tableInfoList);
        ellipses.setBackground(Color.WHITE);
        frame.add(ellipses);
        frame.setSize(frameWidth, frameHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

    public static void saveImg(JFrame frame, String path, String name) {
        String completedPath = path + name;
        // 创建一个BufferedImage对象
        BufferedImage bufferedImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB_PRE);

        // 获取Graphics2D对象
        Graphics2D g2d = bufferedImage.createGraphics();
        frame.paint(g2d); // 将面板绘制到BufferedImage对象中
        g2d.dispose(); // 释放Graphics2D对象

        // 保存图片
        try {
            ImageIO.write(bufferedImage, "png", new File(completedPath));
            System.out.println("\n 已保存 " + name);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("保存图片时出错");
        }
    }

    public static void buildChart(TableInfo tableInfo, List<TableFiled> tableInfoList, String prePath) {
        JFrame frame = drawChart(tableInfo, tableInfoList);
        // 休眠时间，等待绘制完成
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String tblName = tableInfo.getTblName();
        saveImg(frame, prePath, tblName + ".png");
        // 绘制完成销毁
        frame.dispose();
    }

}
