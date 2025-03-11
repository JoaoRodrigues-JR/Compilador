using System;
using System.Linq;
using System.Windows;
using System.Windows.Controls;

namespace Compilador
{
    public partial class MainWindow : Window
    {
        private int _ultimaContagemLinhas = 0;

        public MainWindow()
        {
            InitializeComponent();
            AtualizarNumeroLinha();
            EditorCaixaTexto.TextChanged += EditorCaixaTexto_TextoMudado;
            EditorCaixaTexto.SizeChanged += EditorCaixaTexto_TamanhoMudado;
            EditorScrollViewer.ScrollChanged += EditorScroll_ScrollMudado;
        }

        private void EditorCaixaTexto_TextoMudado(object sender, TextChangedEventArgs e)
        {
            AtualizarNumeroLinha();
            NumeroLinhaScroll.ScrollToVerticalOffset(EditorScrollViewer.VerticalOffset);
        }

        private void EditorCaixaTexto_TamanhoMudado(object sender, SizeChangedEventArgs e)
        {
            AtualizarNumeroLinha();
        }

        private void EditorScroll_ScrollMudado(object sender, ScrollChangedEventArgs e)
        {
            if (NumeroLinhaScroll != null)
            {
                NumeroLinhaScroll.ScrollToVerticalOffset(e.VerticalOffset);
            }
        }

        private void AtualizarNumeroLinha()
        {
            EditorCaixaTexto.UpdateLayout(); // Força a atualização do layout
            int linhaContador = EditorCaixaTexto.LineCount;

            if (linhaContador < 1)
            {
                NumerosLinha.Text = "1";
                return;
            }

            if (linhaContador != _ultimaContagemLinhas)
            {
                _ultimaContagemLinhas = linhaContador;
                NumerosLinha.Text = string.Join(Environment.NewLine, Enumerable.Range(1, linhaContador));
            }
        }

    }
}
