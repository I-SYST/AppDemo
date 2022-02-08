
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
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea4 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend4 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series4 = new System.Windows.Forms.DataVisualization.Charting.Series();
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea5 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend5 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series5 = new System.Windows.Forms.DataVisualization.Charting.Series();
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea6 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend6 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series6 = new System.Windows.Forms.DataVisualization.Charting.Series();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
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
            ((System.ComponentModel.ISupportInitialize)(this.TempChart)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.HumiChart)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.PressChart)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
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
            chartArea4.Name = "ChartArea1";
            this.TempChart.ChartAreas.Add(chartArea4);
            legend4.Name = "Legend1";
            legend4.Position.Auto = false;
            legend4.Position.Height = 11.70569F;
            legend4.Position.Width = 23.46369F;
            legend4.Position.X = 73.53632F;
            legend4.Position.Y = 3F;
            this.TempChart.Legends.Add(legend4);
            this.TempChart.Location = new System.Drawing.Point(23, 557);
            this.TempChart.Name = "TempChart";
            series4.BorderWidth = 2;
            series4.ChartArea = "ChartArea1";
            series4.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series4.Color = System.Drawing.Color.Red;
            series4.Legend = "Legend1";
            series4.Name = "Series1";
            this.TempChart.Series.Add(series4);
            this.TempChart.Size = new System.Drawing.Size(538, 300);
            this.TempChart.TabIndex = 4;
            this.TempChart.Text = "chart1";
            // 
            // HumiChart
            // 
            this.HumiChart.BackColor = System.Drawing.Color.Transparent;
            chartArea5.Name = "ChartArea1";
            this.HumiChart.ChartAreas.Add(chartArea5);
            legend5.Name = "Legend1";
            legend5.Position.Auto = false;
            legend5.Position.Height = 11.70569F;
            legend5.Position.Width = 23.46369F;
            legend5.Position.X = 73.53632F;
            legend5.Position.Y = 3F;
            this.HumiChart.Legends.Add(legend5);
            this.HumiChart.Location = new System.Drawing.Point(591, 557);
            this.HumiChart.Name = "HumiChart";
            series5.BorderWidth = 2;
            series5.ChartArea = "ChartArea1";
            series5.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series5.Color = System.Drawing.Color.Green;
            series5.Legend = "Legend1";
            series5.Name = "Series1";
            this.HumiChart.Series.Add(series5);
            this.HumiChart.Size = new System.Drawing.Size(538, 300);
            this.HumiChart.TabIndex = 5;
            this.HumiChart.Text = "chart2";
            // 
            // PressChart
            // 
            this.PressChart.BackColor = System.Drawing.Color.Transparent;
            chartArea6.BackColor = System.Drawing.Color.White;
            chartArea6.Name = "ChartArea1";
            this.PressChart.ChartAreas.Add(chartArea6);
            legend6.Name = "Legend1";
            legend6.Position.Auto = false;
            legend6.Position.Height = 11.70569F;
            legend6.Position.Width = 23.46369F;
            legend6.Position.X = 73.53632F;
            legend6.Position.Y = 3F;
            this.PressChart.Legends.Add(legend6);
            this.PressChart.Location = new System.Drawing.Point(1161, 557);
            this.PressChart.Name = "PressChart";
            series6.BorderWidth = 2;
            series6.ChartArea = "ChartArea1";
            series6.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series6.Color = System.Drawing.Color.Blue;
            series6.Legend = "Legend1";
            series6.Name = "Series1";
            this.PressChart.Series.Add(series6);
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
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(9F, 20F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1733, 910);
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
    }
}

