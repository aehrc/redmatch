/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

const path = require("path"),
    { CleanWebpackPlugin } = require("clean-webpack-plugin"),
    HtmlWebpackPlugin = require("html-webpack-plugin"),
    MonacoEditorWebpackPlugin = require("monaco-editor-webpack-plugin"),
    MONACO_DIR = path.resolve(__dirname, "./node_modules/monaco-editor");

module.exports = (env, argv) => ({
  entry: "./src/index.tsx",
  output: {
    path: path.resolve(__dirname, "target/site"),
    filename:
        argv.mode === "production"
            ? "scripts/main.[contenthash:8].js"
            : "scripts/main.js"
  },
  devtool: argv.mode === "development" ? "cheap-module-source-map" : false,
  plugins: [
    new CleanWebpackPlugin(),
    new HtmlWebpackPlugin({
      template: "./src/index.html",
      base: argv.mode === "development" ? "http://localhost:3002/" : false
    }),
    new MonacoEditorWebpackPlugin()
  ],
  module: {
    rules: [
      {
        test: /\.(ts|tsx)$/,
        use: {
          loader: "ts-loader"
        },
        exclude: /node_modules/
      },
      {
        test: /\.css$/,
        include: MONACO_DIR,
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.(ttf|eot|woff)$/,
        use: {
          loader: "file-loader",
          options: {
            name: "[hash].[ext]",
            outputPath: "fonts",
            publicPath: "../fonts"
          }
        }
      }
    ]
  },
  resolve: {
    extensions: [".ts", ".tsx", ".js"]
  },
  devServer: {
    historyApiFallback: true
  },
  node: {
    fs: "empty"
  }
});
