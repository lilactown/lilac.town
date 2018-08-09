(ns lilactown.site.workshop)

(defn render []
  [:html
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
   [:head
    [:title "Will Acton"]
    [:script {:type "text/javascript"
              :src "https://unpkg.com/pts/dist/pts.min.js"}]]
   [:body
    [:div
     [:canvas#workshop]
     [:script {:type "text/javascript"}
      "(function(){
  Pts.namespace( window ); // add Pts into scope if needed

  var demoID = \"workshop\"; 

  // create Space and Form
  var space = new CanvasSpace(\"#\"+demoID).setup({ retina: true, resize: true });
  var form = space.getForm();


  // animation
  space.add( () => form.point( space.pointer, 10 ) );

  // start
  space.playOnce(200).bindMouse().bindTouch();

})();"]]]])
