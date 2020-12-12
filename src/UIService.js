import axios from 'axios';


class UIService{


    submitUrl(data){

       return axios.post('https://us-central1-naga-kopalle2.cloudfunctions.net/vision-faas',{  headers : {
        'Content-Type': 'application/json'
    },
       data})

    }

    


}

export default new UIService()