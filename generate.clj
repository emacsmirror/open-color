(ns generate
  (:require [clojure.string :as str]
            [cheshire.core :as json]))

(def defvar-fmt
  "(defvar open-color-%s
  \"%s\")")

(defn- gen-color-defs [[color-name colors]]
  (if (string? colors)
    (format defvar-fmt color-name colors)
    (->> colors
         (map-indexed (fn [i color]
                        (let [numbered-color-name (str color-name "-" i)]
                          (format defvar-fmt numbered-color-name color)))))))

(def code-marker ";;; Code:")

(def provide-marker "\\(provide 'open-color\\)")

(def split-pattern
  (re-pattern
   (format "(?is)(.*%s).*(%s.*)" code-marker provide-marker)))


(defn update-colors [source-colors current-colors]
  (let [elisp-colors (->> source-colors
                          (mapv gen-color-defs)
                          flatten)
        [_ header footer] (re-find split-pattern current-colors)]
    (when (seqable? elisp-colors)
      (->> (concat [header] elisp-colors [footer])
           (str/join "\n\n")))))


(def open-color-el
  "open-color.el")

(def source
  "https://github.com/yeun/open-color/raw/master/open-color.json")

(defn -main []
  (some->> (slurp open-color-el)
           (update-colors (json/parse-string (slurp source)))
           (spit open-color-el)))
