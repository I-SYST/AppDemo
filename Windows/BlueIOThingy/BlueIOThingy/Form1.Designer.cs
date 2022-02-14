
namespace BlueIOThingy
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea6 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend6 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series6 = new System.Windows.Forms.DataVisualization.Charting.Series();
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea7 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend7 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series7 = new System.Windows.Forms.DataVisualization.Charting.Series();
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea8 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend8 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series8 = new System.Windows.Forms.DataVisualization.Charting.Series();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea9 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend9 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series9 = new System.Windows.Forms.DataVisualization.Charting.Series();
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea10 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend10 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series10 = new System.Windows.Forms.DataVisualization.Charting.Series();
            this.scanBtn = new System.Windows.Forms.Button();
            this.stopBtn = new System.Windows.Forms.Button();
            this.listBox1 = new System.Windows.Forms.ListBox();
            this.listBox2 = new System.Windows.Forms.ListBox();
            this.TempChart = new System.Windows.Forms.DataVisualization.Charting.Chart();
            this.HumiChart = new System.Windows.Forms.DataVisualization.Charting.Chart();
            this.PressChart = new System.Windows.Forms.DataVisualization.Charting.Chart();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.tempLabel = new System.Windows.Forms.Label();
            this.humiLabel = new System.Windows.Forms.Label();
            this.pressLabel = new System.Windows.Forms.Label();
            this.rssiLabel = new System.Windows.Forms.Label();
            this.pictureBox1 = new System.Windows.Forms.PictureBox();
            this.label5 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.AQILabel = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.GasLabel = new System.Windows.Forms.Label();
            this.GasChart = new System.Windows.Forms.DataVisualization.Charting.Chart();
            this.AQIChart = new System.Windows.Forms.DataVisualization.Charting.Chart();
            ((System.ComponentModel.ISupportInitialize)(this.TempChart)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.HumiChart)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.PressChart)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.GasChart)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.AQIChart)).BeginInit();
            this.SuspendLayout();
            // 
            // scanBtn
            // 
            this.scanBtn.Location = new System.Drawing.Point(23, 13);
            this.scanBtn.Name = "scanBtn";
            this.scanBtn.Size = new System.Drawing.Size(93, 35);
            this.scanBtn.TabIndex = 0;
            this.scanBtn.Text = "Scan";
            this.scanBtn.UseVisualStyleBackColor = true;
            this.scanBtn.Click += new System.EventHandler(this.scanBtn_Click);
            // 
            // stopBtn
            // 
            this.stopBtn.Location = new System.Drawing.Point(144, 13);
            this.stopBtn.Name = "stopBtn";
            this.stopBtn.Size = new System.Drawing.Size(95, 35);
            this.stopBtn.TabIndex = 1;
            this.stopBtn.Text = "Stop";
            this.stopBtn.UseVisualStyleBackColor = true;
            this.stopBtn.Click += new System.EventHandler(this.stopBtn_Click);
            // 
            // listBox1
            // 
            this.listBox1.FormattingEnabled = true;
            this.listBox1.ItemHeight = 20;
            this.listBox1.Location = new System.Drawing.Point(23, 80);
            this.listBox1.Name = "listBox1";
            this.listBox1.Size = new System.Drawing.Size(557, 424);
            this.listBox1.TabIndex = 2;
            // 
            // listBox2
            // 
            this.listBox2.FormattingEnabled = true;
            this.listBox2.ItemHeight = 20;
            this.listBox2.Location = new System.Drawing.Point(614, 80);
            this.listBox2.Name = "listBox2";
            this.listBox2.Size = new System.Drawing.Size(584, 424);
            this.listBox2.TabIndex = 3;
            // 
            // TempChart
            // 
            this.TempChart.BackColor = System.Drawing.Color.Transparent;
            this.TempChart.BackSecondaryColor = System.Drawing.Color.Silver;
            chartArea6.Name = "ChartArea1";
            this.TempChart.ChartAreas.Add(chartArea6);
            legend6.Name = "Legend1";
            legend6.Position.Auto = false;
            legend6.Position.Height = 11.70569F;
            legend6.Position.Width = 23.46369F;
            legend6.Position.X = 73.53632F;
            legend6.Position.Y = 3F;
            this.TempChart.Legends.Add(legend6);
            this.TempChart.Location = new System.Drawing.Point(23, 557);
            this.TempChart.Name = "TempChart";
            series6.BorderWidth = 2;
            series6.ChartArea = "ChartArea1";
            series6.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series6.Color = System.Drawing.Color.Red;
            series6.Legend = "Legend1";
            series6.Name = "Series1";
            this.TempChart.Series.Add(series6);
            this.TempChart.Size = new System.Drawing.Size(538, 300);
            this.TempChart.TabIndex = 4;
            this.TempChart.Text = "chart1";
            // 
            // HumiChart
            // 
            this.HumiChart.BackColor = System.Drawing.Color.Transparent;
            chartArea7.Name = "ChartArea1";
            this.HumiChart.ChartAreas.Add(chartArea7);
            legend7.Name = "Legend1";
            legend7.Position.Auto = false;
            legend7.Position.Height = 11.70569F;
            legend7.Position.Width = 23.46369F;
            legend7.Position.X = 73.53632F;
            legend7.Position.Y = 3F;
            this.HumiChart.Legends.Add(legend7);
            this.HumiChart.Location = new System.Drawing.Point(591, 557);
            this.HumiChart.Name = "HumiChart";
            series7.BorderWidth = 2;
            series7.ChartArea = "ChartArea1";
            series7.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series7.Color = System.Drawing.Color.Green;
            series7.Legend = "Legend1";
            series7.Name = "Series1";
            this.HumiChart.Series.Add(series7);
            this.HumiChart.Size = new System.Drawing.Size(538, 300);
            this.HumiChart.TabIndex = 5;
            this.HumiChart.Text = "chart2";
            // 
            // PressChart
            // 
            this.PressChart.BackColor = System.Drawing.Color.Transparent;
            chartArea8.BackColor = System.Drawing.Color.White;
            chartArea8.Name = "ChartArea1";
            this.PressChart.ChartAreas.Add(chartArea8);
            legend8.Name = "Legend1";
            legend8.Position.Auto = false;
            legend8.Position.Height = 11.70569F;
            legend8.Position.Width = 23.46369F;
            legend8.Position.X = 73.53632F;
            legend8.Position.Y = 3F;
            this.PressChart.Legends.Add(legend8);
            this.PressChart.Location = new System.Drawing.Point(1161, 557);
            this.PressChart.Name = "PressChart";
            series8.BorderWidth = 2;
            series8.ChartArea = "ChartArea1";
            series8.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series8.Color = System.Drawing.Color.Blue;
            series8.Legend = "Legend1";
            series8.Name = "Series1";
            this.PressChart.Series.Add(series8);
            this.PressChart.Size = new System.Drawing.Size(538, 300);
            this.PressChart.TabIndex = 6;
            this.PressChart.Text = "chart3";
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(1252, 352);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(104, 20);
            this.label1.TabIndex = 7;
            this.label1.Text = "Temperature:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(1252, 393);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(74, 20);
            this.label2.TabIndex = 8;
            this.label2.Text = "Humidity:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(1252, 432);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(76, 20);
            this.label3.TabIndex = 9;
            this.label3.Text = "Pressure:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(1252, 477);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(52, 20);
            this.label4.TabIndex = 10;
            this.label4.Text = "RSSI:";
            // 
            // tempLabel
            // 
            this.tempLabel.AutoSize = true;
            this.tempLabel.Location = new System.Drawing.Point(1385, 352);
            this.tempLabel.Name = "tempLabel";
            this.tempLabel.Size = new System.Drawing.Size(18, 20);
            this.tempLabel.TabIndex = 11;
            this.tempLabel.Text = "0";
            // 
            // humiLabel
            // 
            this.humiLabel.AutoSize = true;
            this.humiLabel.Location = new System.Drawing.Point(1386, 393);
            this.humiLabel.Name = "humiLabel";
            this.humiLabel.Size = new System.Drawing.Size(18, 20);
            this.humiLabel.TabIndex = 12;
            this.humiLabel.Text = "0";
            // 
            // pressLabel
            // 
            this.pressLabel.AutoSize = true;
            this.pressLabel.Location = new System.Drawing.Point(1386, 431);
            this.pressLabel.Name = "pressLabel";
            this.pressLabel.Size = new System.Drawing.Size(18, 20);
            this.pressLabel.TabIndex = 13;
            this.pressLabel.Text = "0";
            // 
            // rssiLabel
            // 
            this.rssiLabel.AutoSize = true;
            this.rssiLabel.Location = new System.Drawing.Point(1386, 476);
            this.rssiLabel.Name = "rssiLabel";
            this.rssiLabel.Size = new System.Drawing.Size(18, 20);
            this.rssiLabel.TabIndex = 14;
            this.rssiLabel.Text = "0";
            // 
            // pictureBox1
            // 
            this.pictureBox1.Image = ((System.Drawing.Image)(resources.GetObject("pictureBox1.Image")));
            this.pictureBox1.InitialImage = ((System.Drawing.Image)(resources.GetObject("pictureBox1.InitialImage")));
            this.pictureBox1.Location = new System.Drawing.Point(1256, 32);
            this.pictureBox1.Name = "pictureBox1";
            this.pictureBox1.Size = new System.Drawing.Size(443, 173);
            this.pictureBox1.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBox1.TabIndex = 15;
            this.pictureBox1.TabStop = false;
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Font = new System.Drawing.Font("Microsoft Sans Serif", 20F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label5.ForeColor = System.Drawing.SystemColors.Highlight;
            this.label5.Location = new System.Drawing.Point(1352, 218);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(272, 46);
            this.label5.TabIndex = 16;
            this.label5.Text = "BlueIOThingy";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(1563, 352);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(41, 20);
            this.label6.TabIndex = 17;
            this.label6.Text = "AQI:";
            // 
            // AQILabel
            // 
            this.AQILabel.AutoSize = true;
            this.AQILabel.Location = new System.Drawing.Point(1625, 352);
            this.AQILabel.Name = "AQILabel";
            this.AQILabel.Size = new System.Drawing.Size(18, 20);
            this.AQILabel.TabIndex = 18;
            this.AQILabel.Text = "0";
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(1563, 383);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(43, 20);
            this.label7.TabIndex = 19;
            this.label7.Text = "Gas:";
            // 
            // GasLabel
            // 
            this.GasLabel.AutoSize = true;
            this.GasLabel.Location = new System.Drawing.Point(1625, 383);
            this.GasLabel.Name = "GasLabel";
            this.GasLabel.Size = new System.Drawing.Size(18, 20);
            this.GasLabel.TabIndex = 20;
            this.GasLabel.Text = "0";
            // 
            // GasChart
            // 
            this.GasChart.BackColor = System.Drawing.Color.Transparent;
            chartArea9.Name = "ChartArea1";
            this.GasChart.ChartAreas.Add(chartArea9);
            legend9.Name = "Legend1";
            legend9.Position.Auto = false;
            legend9.Position.Height = 11.70569F;
            legend9.Position.Width = 23.46369F;
            legend9.Position.X = 73.53632F;
            legend9.Position.Y = 3F;
            this.GasChart.Legends.Add(legend9);
            this.GasChart.Location = new System.Drawing.Point(591, 874);
            this.GasChart.Name = "GasChart";
            series9.BorderWidth = 2;
            series9.ChartArea = "ChartArea1";
            series9.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series9.Color = System.Drawing.Color.Magenta;
            series9.Legend = "Legend1";
            series9.Name = "Series1";
            this.GasChart.Series.Add(series9);
            this.GasChart.Size = new System.Drawing.Size(538, 300);
            this.GasChart.TabIndex = 22;
            this.GasChart.Text = "chart2";
            // 
            // AQIChart
            // 
            this.AQIChart.BackColor = System.Drawing.Color.Transparent;
            this.AQIChart.BackSecondaryColor = System.Drawing.Color.Silver;
            chartArea10.Name = "ChartArea1";
            this.AQIChart.ChartAreas.Add(chartArea10);
            legend10.Name = "Legend1";
            legend10.Position.Auto = false;
            legend10.Position.Height = 11.70569F;
            legend10.Position.Width = 23.46369F;
            legend10.Position.X = 73.53632F;
            legend10.Position.Y = 3F;
            this.AQIChart.Legends.Add(legend10);
            this.AQIChart.Location = new System.Drawing.Point(23, 874);
            this.AQIChart.Name = "AQIChart";
            series10.BorderWidth = 2;
            series10.ChartArea = "ChartArea1";
            series10.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series10.Color = System.Drawing.Color.Gold;
            series10.Legend = "Legend1";
            series10.Name = "Series1";
            this.AQIChart.Series.Add(series10);
            this.AQIChart.Size = new System.Drawing.Size(538, 300);
            this.AQIChart.TabIndex = 21;
            this.AQIChart.Text = "chart1";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(9F, 20F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1733, 1186);
            this.Controls.Add(this.GasChart);
            this.Controls.Add(this.AQIChart);
            this.Controls.Add(this.GasLabel);
            this.Controls.Add(this.label7);
            this.Controls.Add(this.AQILabel);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.pictureBox1);
            this.Controls.Add(this.rssiLabel);
            this.Controls.Add(this.pressLabel);
            this.Controls.Add(this.humiLabel);
            this.Controls.Add(this.tempLabel);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.PressChart);
            this.Controls.Add(this.HumiChart);
            this.Controls.Add(this.TempChart);
            this.Controls.Add(this.listBox2);
            this.Controls.Add(this.listBox1);
            this.Controls.Add(this.stopBtn);
            this.Controls.Add(this.scanBtn);
            this.Name = "Form1";
            this.Text = "BlueIOThingy";
            ((System.ComponentModel.ISupportInitialize)(this.TempChart)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.HumiChart)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.PressChart)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.GasChart)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.AQIChart)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button scanBtn;
        private System.Windows.Forms.Button stopBtn;
        private System.Windows.Forms.ListBox listBox1;
        private System.Windows.Forms.ListBox listBox2;
        private System.Windows.Forms.DataVisualization.Charting.Chart TempChart;
        private System.Windows.Forms.DataVisualization.Charting.Chart HumiChart;
        private System.Windows.Forms.DataVisualization.Charting.Chart PressChart;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label tempLabel;
        private System.Windows.Forms.Label humiLabel;
        private System.Windows.Forms.Label pressLabel;
        private System.Windows.Forms.Label rssiLabel;
        private System.Windows.Forms.PictureBox pictureBox1;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label AQILabel;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Label GasLabel;
        private System.Windows.Forms.DataVisualization.Charting.Chart GasChart;
        private System.Windows.Forms.DataVisualization.Charting.Chart AQIChart;
    }
}

