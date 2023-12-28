package com.riccardograziosi.slimlauncher.utils

interface IPublisher{
    fun attachSubscriber(s: ISubscriber)
    fun detachSubscriber(s: ISubscriber)
}